package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.ResolvedMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.FetchConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
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
import org.jspecify.annotations.NonNull;

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
    public @NonNull ExecutionHolder executionHolder() {
        return executionHolder;
    }

    @Override
    public @NonNull ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    @Override
    public @NonNull Uni<Consumer> addIfAbsent(@NonNull String stream, @NonNull String name, @NonNull ConsumerConfiguration configuration) {
        return addIfAbsent(stream, name, consumerConfigurationMapper.of(name, configuration));
    }

    @Override
    public @NonNull Uni<Consumer> addIfAbsent(@NonNull String stream, @NonNull String name, @NonNull PushConsumerConfiguration configuration) {
        return addIfAbsent(stream, name, consumerConfigurationMapper.of(name, configuration));
    }

    @Override
    public @NonNull Uni<Consumer> addIfAbsent(@NonNull String stream, @NonNull String name, @NonNull PullConsumerConfiguration configuration) {
        return addIfAbsent(stream, name, consumerConfigurationMapper.of(name, configuration));
    }

    @Override
    public @NonNull Uni<Consumer> get(@NonNull final String stream, @NonNull final String consumerName) {
        return withContext(context -> context.executeBlocking(withConnection(connection ->
                connection.consumerInfo(stream, consumerName)
                        .onItem().transform(consumerMapper::of)
                        .onFailure().transform(ClientException::new))));
    }

    @Override
    public @NonNull Multi<String> names(@NonNull final String streamName) {
        return withSubscriberConnection(connection -> connection.consumerNames(streamName))
                .onFailure().transform(ClientException::new);
    }

    @Override
    public @NonNull Uni<Void> delete(@NonNull final String streamName, @NonNull final String consumerName) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.deleteConsumer(streamName, consumerName)
                .onItem().transformToUni(deleted -> deleted ? Uni.createFrom().voidItem() : Uni.createFrom().failure(new ConsumerNotDeletedException(streamName, consumerName)))
                .onFailure().transform(ClientException::new))));
    }

    @Override
    public @NonNull Uni<Void> pause(@NonNull final String streamName, @NonNull final String consumerName, @NonNull final ZonedDateTime pauseUntil) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.pauseConsumer(streamName, consumerName, pauseUntil)
                .onItem().transformToUni(response -> response.isPaused() ? Uni.createFrom().voidItem() : Uni.createFrom().failure(() -> new ClientException(String.format("Unable to pause consumer %s in stream %s", consumerName, streamName))))
                .onFailure().transform(ClientException::new))));
    }

    @Override
    public @NonNull Uni<Void> resume(@NonNull final String streamName, @NonNull final String consumerName) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.resumeConsumer(streamName, consumerName)
                .onItem().transformToUni(response -> response ? Uni.createFrom().voidItem() : Uni.createFrom().failure(() -> new ClientException(String.format("Unable to resume consumer %s in stream %s", consumerName, streamName))))
                .onFailure().transform(ClientException::new))));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <T> Uni<Message<T>> next(@NonNull final String stream, @NonNull final String consumer, @NonNull final ConsumerConfiguration configuration, @NonNull final Duration timeout) {
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

    @Override
    public <T> @NonNull Multi<Message<T>> fetch(@NonNull final String stream,
                                                @NonNull final String consumer,
                                                @NonNull final FetchConsumerConfiguration configuration) {
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
    public <T> @NonNull Uni<Message<T>> resolve(@NonNull String streamName, long sequence) {
        return withContext(context -> withConnection(connection -> connection.resolve(streamName, sequence)
                .onItem().<Message<T>>transform(messageInfo -> new ResolvedMessage<>(messageInfo, payloadMapper.<T>of(messageInfo).orElse(null)))))
                .onFailure().transform(ClientException::new);
    }

    @Override
    public @NonNull <T> Multi<Message<T>> subscribe(@NonNull String stream, @NonNull String consumer, @NonNull PullConsumerConfiguration configuration) {
        final var tracer = tracerFactory.<T> create(TracerType.Subscribe);
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
    public @NonNull <T> Multi<Message<T>> subscribe(@NonNull String stream, @NonNull String consumer, @NonNull PushConsumerConfiguration configuration) {
        final var tracer = tracerFactory.<T> create(TracerType.Subscribe);
        return withContext(context -> withSubscriberConnection(connection ->
                connection.subscribe(stream, consumer, configuration.pushConfiguration())
                        .emitOn(context::runOnContext)
                        .onItem().transformToUniAndMerge(message -> transformMessage(message, configuration.consumerConfiguration(), context))
                        .onItem().transform(message -> (Message<T>) message))
                        .onItem().transformToUniAndMerge(message -> tracer.withTrace(message, msg -> msg)));
    }

    private @NonNull Uni<Consumer> addIfAbsent(@NonNull final String stream, @NonNull final String name, final io.nats.client.api.ConsumerConfiguration configuration) {
        return withContext(context -> context.executeBlocking(withConnection(connection ->
                connection.consumerInfo(stream, name)
                        .onItem().ifNull().switchTo(() -> connection.createConsumer(stream, configuration))
                        .onItem().transform(consumerMapper::of)
                        .onFailure().transform(ClientException::new)
        )));
    }

    private <T> @NonNull Uni<Message<T>> transformMessage(final io.nats.client.Message message,
                                                          @NonNull final ConsumerConfiguration<T> configuration,
                                                          @NonNull final Context context) {
        return Uni.createFrom()
                .item(Unchecked.supplier(
                        () -> messageMapper.of(message, configuration, context)));
    }
}
