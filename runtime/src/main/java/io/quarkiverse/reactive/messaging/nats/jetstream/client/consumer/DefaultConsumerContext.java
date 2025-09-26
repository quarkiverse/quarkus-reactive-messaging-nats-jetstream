package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.ResolvedMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.context.ContextAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.ConsumerConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.ConsumerMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.MessageMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.AttachContextTraceSupplier;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.vertx.mutiny.core.Context;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultConsumerContext implements ConsumerContext, ConnectionAware, ContextAware {
    private final ExecutionHolder executionHolder;
    private final ConnectionFactory connectionFactory;
    private final ConsumerConfigurationMapper consumerConfigurationMapper;
    private final ConsumerMapper consumerMapper;
    private final TracerFactory tracerFactory;
    private final MessageMapper messageMapper;
    private final PayloadMapper payloadMapper;

    public DefaultConsumerContext(final ExecutionHolder executionHolder,
                                  final ConnectionFactory connectionFactory,
                                  final ConsumerConfigurationMapper consumerConfigurationMapper,
                                  final ConsumerMapper consumerMapper,
                                  final TracerFactory tracerFactory,
                                  final MessageMapper messageMapper,
                                  final PayloadMapper payloadMapper) {
        this.executionHolder = executionHolder;
        this.connectionFactory = connectionFactory;
        this.consumerConfigurationMapper = consumerConfigurationMapper;
        this.consumerMapper = consumerMapper;
        this.tracerFactory = tracerFactory;
        this.messageMapper = messageMapper;
        this.payloadMapper = payloadMapper;
    }

    @Override
    public ExecutionHolder executionHolder() {
        return executionHolder;
    }

    @Override
    public ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    @Override
    public <T> Uni<Consumer> addIfAbsent(String stream, String name, ConsumerConfiguration<T> configuration) {
        return addIfAbsent(stream, name, consumerConfigurationMapper.map(name, configuration));
    }

    @Override
    public <T> Uni<Consumer> addIfAbsent(String stream, String name, PushConsumerConfiguration<T> configuration) {
        return addIfAbsent(stream, name, consumerConfigurationMapper.map(name, configuration));
    }

    @Override
    public <T> Uni<Consumer> addIfAbsent(String stream, String name, PullConsumerConfiguration<T> configuration) {
        return addIfAbsent(stream, name, consumerConfigurationMapper.map(name, configuration));
    }

    @Override
    public Uni<Consumer> get(final String stream, final String consumerName) {
        return withContext(context -> context.executeBlocking(withConnection(connection ->
                connection.consumerInfo(stream, consumerName)
                        .onItem().transform(consumerMapper::of)
                        .onFailure().transform(ClientException::new))));
    }

    @Override
    public Multi<String> names(final String streamName) {
        return withSubscriberConnection(connection -> connection.consumerNames(streamName))
                .onFailure().transform(ClientException::new);
    }

    @Override
    public Uni<Void> delete(final String streamName, final String consumerName) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.deleteConsumer(streamName, consumerName)
                .onItem().transformToUni(deleted -> deleted ? Uni.createFrom().voidItem() : Uni.createFrom().failure(new ConsumerNotDeletedException(streamName, consumerName)))
                .onFailure().transform(ClientException::new))));
    }

    @Override
    public Uni<Void> pause(final String streamName, final String consumerName, final ZonedDateTime pauseUntil) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.pauseConsumer(streamName, consumerName, pauseUntil)
                .onItem().transformToUni(response -> response.isPaused() ? Uni.createFrom().voidItem() : Uni.createFrom().failure(() -> new ClientException(String.format("Unable to pause consumer %s in stream %s", consumerName, streamName))))
                .onFailure().transform(ClientException::new))));
    }

    @Override
    public Uni<Void> resume(final String streamName, final String consumerName) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.resumeConsumer(streamName, consumerName)
                .onItem().transformToUni(response -> response ? Uni.createFrom().voidItem() : Uni.createFrom().failure(() -> new ClientException(String.format("Unable to resume consumer %s in stream %s", consumerName, streamName))))
                .onFailure().transform(ClientException::new))));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Uni<Message<T>> next(final String stream, final String consumer, final ConsumerConfiguration configuration, final Duration timeout) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.next(stream, consumer, timeout)
                .onItem().ifNull().failWith(MessageNotFoundException::new)
                .onItem().ifNotNull().transformToUni(message -> transformMessage(message, configuration, context))
                .onItem().transform(message -> (Message<T>) message)
                .onItem().transformToUni(message -> tracerFactory.<T>create(TracerType.Subscribe).withTrace(message,
                        new AttachContextTraceSupplier<>()))
                .onFailure().transform(failure -> {
                    if (failure instanceof MessageNotFoundException) {
                        return failure;
                    }
                    return new FetchException(failure);
                }))));
    }

    @SuppressWarnings("resource")
    @Override
    public <T> Multi<Message<T>> fetch(String stream, String consumer, FetchConsumerConfiguration<T> configuration) {
        ExecutorService executor = Executors.newSingleThreadExecutor(ConsumerWorkerThread::new);
        return withContext(context -> withSubscriberConnection(connection ->
                connection.fetch(stream, consumer, configuration.fetchConfiguration())
                        .runSubscriptionOn(executor)
                        .emitOn(context::runOnContext)
                        .onItem().transformToUniAndMerge(message -> transformMessage(message, configuration.consumerConfiguration(), context))
                        .onItem().transform(message -> (Message<T>) message)
                        .onItem().transformToUniAndMerge(message -> tracerFactory.<T>create(TracerType.Subscribe).withTrace(message, new AttachContextTraceSupplier<>()))
                        .onFailure().transform(ClientException::new)
        ));
    }

    @Override
    public <T> Uni<Message<T>> resolve(String streamName, long sequence) {
        return withContext(context -> withConnection(connection -> connection.resolve(streamName, sequence)
                .onItem().<Message<T>>transform(messageInfo -> new ResolvedMessage<>(messageInfo, payloadMapper.<T>of(messageInfo).orElse(null)))))
                .onFailure().transform(ClientException::new);
    }

    @Override
    public <T> Multi<Message<T>> subscribe(String stream, String consumer, PullConsumerConfiguration configuration) {
        final var tracer = tracerFactory.<T>create(TracerType.Subscribe);
        ExecutorService pullExecutor = Executors.newSingleThreadExecutor(ConsumerWorkerThread::new);
        return withContext(context -> withSubscriberConnection(connection ->
                connection.subscribe(stream, consumer, configuration.pullConfiguration())
                        .runSubscriptionOn(pullExecutor)
                        .emitOn(context::runOnContext)
                        .onItem().transformToUniAndMerge(message -> transformMessage(message, configuration.consumerConfiguration(), context))
                        .onItem().transform(message -> (Message<T>) message))
                .onItem().transformToUniAndMerge(message -> tracer.withTrace(message, msg -> msg)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Multi<Message<T>> subscribe(String stream, String consumer, PushConsumerConfiguration configuration) {
        final var tracer = tracerFactory.<T>create(TracerType.Subscribe);
        return withContext(context -> withSubscriberConnection(connection ->
                connection.subscribe(stream, consumer, configuration.pushConfiguration())
                        .emitOn(context::runOnContext)
                        .onItem().transformToUniAndMerge(message -> transformMessage(message, configuration.consumerConfiguration(), context))
                        .onItem().transform(message -> (Message<T>) message))
                .onItem().transformToUniAndMerge(message -> tracer.withTrace(message, msg -> msg)));
    }

    private Uni<Consumer> addIfAbsent(final String stream, final String name, final io.nats.client.api.ConsumerConfiguration configuration) {
        return withContext(context -> context.executeBlocking(withConnection(connection ->
                connection.consumerInfo(stream, name)
                        .onItem().ifNull().switchTo(() -> connection.createConsumer(stream, configuration))
                        .onItem().transform(consumerMapper::of)
                        .onFailure().transform(ClientException::new)
        )));
    }

    private <T> Uni<Message<T>> transformMessage(final io.nats.client.Message message,
                                                 final ConsumerConfiguration<T> configuration,
                                                 final Context context) {
        return Uni.createFrom()
                .item(Unchecked.supplier(
                        () -> messageMapper.of(message, configuration, context)));
    }
}
