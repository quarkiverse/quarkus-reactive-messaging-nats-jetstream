package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import io.nats.client.*;
import io.nats.client.api.ConsumerInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.ResolvedMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.JetStreamAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.context.ContextAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.MessageMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamContextAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.AttachContextTraceSupplier;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.vertx.mutiny.core.Context;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public record ConsumerAwareImpl(ExecutionHolder executionHolder,
                                ConsumerConfigurationMapper consumerConfigurationMapper,
                                ConsumerMapper consumerMapper, TracerFactory tracerFactory,
                                MessageMapper messageMapper,
                                PayloadMapper payloadMapper,
                                Connection connection) implements ConsumerAware, ContextAware, JetStreamAware, StreamContextAware {

    @Override
    public <T> Uni<Consumer> addConsumerIfAbsent(final ConsumerConfiguration<T> configuration) {
        return addConsumerIfAbsent(configuration.stream(), consumerConfigurationMapper.map(configuration))
                .onFailure().transform(ClientException::new);
    }

    @Override
    public <T> Uni<Consumer> addConsumerIfAbsent(final ConsumerConfiguration<T> configuration, PushConfiguration pushConfiguration) {
        return addConsumerIfAbsent(configuration.stream(), consumerConfigurationMapper.map(configuration, pushConfiguration))
                .onFailure().transform(ClientException::new);
    }

    @Override
    public <T> Uni<Consumer> addConsumerIfAbsent(final ConsumerConfiguration<T> configuration, PullConfiguration pullConfiguration) {
        return addConsumerIfAbsent(configuration.stream(), consumerConfigurationMapper.map(configuration, pullConfiguration))
                .onFailure().transform(ClientException::new);
    }

    @Override
    public Uni<Consumer> consumer(final String stream, final String consumerName) {
        return withContext(context -> context.executeBlocking(consumerInfo(stream, consumerName)
                .onItem().transform(consumerMapper::map)
                .onFailure().transform(ClientException::new)));
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public Multi<String> consumerNames(final String streamName) {
        return jetStreamManagement().onItem()
                .transformToMulti(jetStreamManagement -> Multi.createFrom()
                        .items(Unchecked.supplier(() -> jetStreamManagement.getConsumerNames(streamName).stream()))
                        .onFailure().transform(ClientException::new));
    }

    @Override
    public Uni<Void> deleteConsumer(final String streamName, final String consumerName) {
        return withContext(context -> context.executeBlocking(jetStreamManagement().onItem()
                .transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.deleteConsumer(streamName, consumerName))))
                .onItem().transformToUni(deleted -> deleted ? Uni.createFrom().voidItem() : Uni.createFrom().failure(() -> new ConsumerNotDeletedException(streamName, consumerName)))
                .onFailure().transform(ClientException::new)));
    }

    @Override
    public Uni<Void> pauseConsumer(final String streamName, final String consumerName, final ZonedDateTime pauseUntil) {
        return withContext(context -> context.executeBlocking(jetStreamManagement()
                .onItem().transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.pauseConsumer(streamName, consumerName, pauseUntil))))
                .onItem().transformToUni(response -> response.isPaused() ? Uni.createFrom().voidItem() : Uni.createFrom().failure(() -> new ConsumerNotPausedException(streamName, consumerName)))
                .onFailure().transform(ClientException::new)));
    }

    @Override
    public Uni<Void> resumeConsumer(final String streamName, final String consumerName) {
        return withContext(context -> context.executeBlocking(jetStreamManagement()
                .onItem()
                .transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.resumeConsumer(streamName, consumerName))))
                .onItem()
                .transformToUni(response -> response ? Uni.createFrom().voidItem() : Uni.createFrom().failure(() -> new ConsumerNotResumedException(streamName, consumerName)))
                .onFailure().transform(ClientException::new)));
    }

    @Override
    public <T> Uni<Message<T>> next(final ConsumerConfiguration<T> configuration, final Duration timeout) {
        return withContext(context -> context.executeBlocking(next(configuration.stream(), configuration.name(), timeout)
                .runSubscriptionOn(context::runOnContext)
                .onItem().ifNotNull().transformToUni(message -> transformMessage(message, configuration, context))
                .onItem().ifNotNull().transformToUni(message -> tracerFactory.<T>create(TracerType.Subscribe).withTrace(message, new AttachContextTraceSupplier<>()))
                .onFailure().transform(ClientException::new)));
    }

    @SuppressWarnings({"resource", "ReactiveStreamsUnusedPublisher"})
    @Override
    public <T> Multi<Message<T>> fetch(final ConsumerConfiguration<T> configuration, final FetchConfiguration fetchConfiguration) {
        ExecutorService executor = Executors.newSingleThreadExecutor(ConsumerWorkerThread::new);
        return withContext(context -> consumerContext(configuration.stream(), configuration.name())
                .onItem().transformToMulti(consumerContext -> fetch(consumerContext, fetchConfiguration)
                        .runSubscriptionOn(executor)
                        .emitOn(context::runOnContext)
                        .onItem().transformToUniAndMerge(message -> transformMessage(message, configuration, context))
                        .onItem().transformToUniAndMerge(message -> tracerFactory.<T>create(TracerType.Subscribe).withTrace(message, new AttachContextTraceSupplier<>()))
                        .onFailure().transform(ClientException::new)));
    }

    @Override
    public <T> Uni<Message<T>> resolve(final String streamName, final long sequence) {
        return withContext(context -> context.executeBlocking(streamContext(streamName)
                .onItem().transformToUni(streamContext -> Uni.createFrom().item(Unchecked.supplier(() -> streamContext.getMessage(sequence))))
                .onItem().<Message<T>>transform(messageInfo -> new ResolvedMessage<>(messageInfo, payloadMapper.map(messageInfo)))))
                .onFailure().transform(ClientException::new);
    }

    @Override
    public <T> Multi<Message<T>> subscribe(final ConsumerConfiguration<T> configuration, final PullConfiguration pullConfiguration) {
        return subscribe(configuration, pullConfiguration, new ConsumerListenerImpl<>());
    }

    @Override
    public <T> Multi<Message<T>> subscribe(final ConsumerConfiguration<T> configuration, final PushConfiguration pushConfiguration) {
        return subscribe(configuration, pushConfiguration, new ConsumerListenerImpl<>());
    }

    @SuppressWarnings("resource")
    @Override
    public <T> Multi<Message<T>> subscribe(ConsumerConfiguration<T> configuration, PullConfiguration pullConfiguration, ConsumerListener<T> listener) {
        final var tracer = tracerFactory.<T>create(TracerType.Subscribe);
        ExecutorService pullExecutor = Executors.newSingleThreadExecutor(ConsumerWorkerThread::new);
        return withContext(context -> subscribe(configuration.stream(), configuration.name(), pullConfiguration)
                .runSubscriptionOn(pullExecutor)
                .emitOn(context::runOnContext)
                .onItem().transformToUniAndMerge(message -> transformMessage(message, configuration, context))
                .onItem().transformToUniAndMerge(message -> tracer.withTrace(message, msg -> msg)))
                .onItem().invoke(listener::onMessage)
                .onFailure().invoke(listener::onError)
                .onFailure().transform(ClientException::new);
    }

    @Override
    public <T> Multi<Message<T>> subscribe(ConsumerConfiguration<T> configuration, PushConfiguration pushConfiguration, ConsumerListener<T> listener) {
        final var tracer = tracerFactory.<T>create(TracerType.Subscribe);
        return withContext(context -> Multi.createFrom().<io.nats.client.Message> emitter(emitter -> {
                    try {
                        final var jetStream = connection.jetStream();
                        final var dispatcher = connection.createDispatcher();
                        final var pushOptions = pushSubscribeOptions(configuration.stream(), configuration.name(), pushConfiguration.ordered());
                        jetStream.subscribe(
                                null, dispatcher,
                                emitter::emit,
                                false,
                                pushOptions);
                    } catch (Exception e) {
                        emitter.fail(e);
                    }
                })
                .emitOn(context::runOnContext)
                .onItem().transformToUniAndMerge(message -> transformMessage(message, configuration, context))
                .onItem().transformToUniAndMerge(message -> tracer.withTrace(message, msg -> msg)))
                .onItem().invoke(listener::onMessage)
                .onFailure().invoke(listener::onError)
                .onFailure().transform(ClientException::new);
    }

    private Uni<Consumer> addConsumerIfAbsent(final String stream, final io.nats.client.api.ConsumerConfiguration configuration) {
        return withContext(context -> context.executeBlocking(consumerInfo(stream, configuration.getName())
                .onItem().ifNull().switchTo(() -> createConsumer(stream, configuration))
                .onItem().transform(consumerMapper::map)));
    }

    private Uni<ConsumerInfo> createConsumer(final String stream, final io.nats.client.api.ConsumerConfiguration configuration) {
        return jetStreamManagement()
                .onItem()
                .transformToUni(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.createConsumer(stream, configuration))));
    }

    private <T> Uni<Message<T>> transformMessage(final io.nats.client.Message message,
                                                 final ConsumerConfiguration<T> configuration,
                                                 final Context context) {
        return Uni.createFrom()
                .item(Unchecked.supplier(
                        () -> messageMapper.map(message, configuration, context)));
    }

    private Uni<ConsumerInfo> consumerInfo(final String stream, final String consumer) {
        return jetStreamManagement()
                .onItem()
                .transformToUni(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.getConsumerNames(stream)))
                        .onItem().transformToUni(consumerNames -> {
                            if (consumerNames.contains(consumer)) {
                                return Uni.createFrom()
                                        .item(Unchecked.supplier(() -> jetStreamManagement.getConsumerInfo(stream, consumer)));
                            } else {
                                return Uni.createFrom().nullItem();
                            }
                        }));
    }

    private Uni<io.nats.client.ConsumerContext> consumerContext(final String stream, final String consumer) {
        return streamContext(stream)
                .onItem().transformToUni(streamContext -> Uni.createFrom().item(Unchecked.supplier(() -> streamContext.getConsumerContext(consumer))));
    }

    private Uni<io.nats.client.Message> next(final String stream, final String consumer, final Duration timeout) {
        return consumerContext(stream, consumer)
                .onItem()
                .transformToUni(consumerContext -> next(consumerContext, timeout));
    }

    private Uni<io.nats.client.Message> next(final ConsumerContext consumerContext, final Duration timeout) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                emitter.complete(consumerContext.next(timeout));
            } catch (JetStreamStatusException e) {
                emitter.fail(e);
            } catch (IllegalStateException | InterruptedException e) {
                emitter.complete(null);
            } catch (Exception e) {
                emitter.fail(e);
            }
        });
    }

    private Multi<io.nats.client.Message> fetch(final ConsumerContext consumerContext, FetchConfiguration configuration) {
        return Multi.createFrom().emitter(emitter -> {
            try {
                try (final var fetchConsumer = fetchConsumer(consumerContext, configuration)) {
                    var message = fetchConsumer.nextMessage();
                    while (message != null) {
                        emitter.emit(message);
                        message = fetchConsumer.nextMessage();
                    }
                    emitter.complete();
                }
            } catch (Exception failure) {
                emitter.fail(failure);
            }
        });
    }

    private FetchConsumer fetchConsumer(final ConsumerContext consumerContext, final FetchConfiguration configuration) throws IOException, JetStreamApiException {
        if (configuration.timeout().isEmpty()) {
            return consumerContext.fetch(
                    FetchConsumeOptions.builder().maxMessages(configuration.batchSize()).noWait().build());
        } else {
            return consumerContext
                    .fetch(FetchConsumeOptions.builder().maxMessages(configuration.batchSize())
                            .expiresIn(configuration.timeout().get().toMillis()).build());
        }
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    private Multi<io.nats.client.Message> subscribe(final String stream, final String consumer, final PullConfiguration configuration) {
        if (configuration.batchSize() <= 1) {
            return consumerContext(stream, consumer)
                    .onItem().transformToMulti(consumerContext -> Multi.createBy().repeating()
                            .uni(() -> next(consumerContext, configuration.maxExpires()))
                            .whilst(message -> true)
                            .flatMap(message -> message != null ? Multi.createFrom().item(message) : Multi.createFrom().empty()));
        } else {
            return reader(stream, consumer, configuration)
                    .onItem().transformToMulti(reader -> Multi.createBy().repeating()
                            .uni(() -> next(reader, configuration.maxExpires()))
                            .whilst(message -> true)
                            .flatMap(message -> message != null ? Multi.createFrom().item(message) : Multi.createFrom().empty()));
        }
    }

    private Uni<JetStreamReader> reader(final String stream, final String consumer, final PullConfiguration configuration) {
        return jetStream()
                .onItem()
                .transformToUni(jetStream -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStream.subscribe(null, pullSubscribeOptions(stream, consumer)))))
                .onItem()
                .transformToUni(subscription -> Uni.createFrom().item(
                        Unchecked.supplier(() -> subscription.reader(configuration.batchSize(), configuration.rePullAt()))));
    }

    private Uni<io.nats.client.Message> next(final JetStreamReader reader, final Duration timeout) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                emitter.complete(reader.nextMessage(timeout));
            } catch (JetStreamStatusException e) {
                emitter.fail(e);
            } catch (IllegalStateException | InterruptedException e) {
                emitter.complete(null);
            } catch (Exception e) {
                emitter.fail(e);
            }
        });
    }

    private PushSubscribeOptions pushSubscribeOptions(final String stream,
                                                      final String consumer,
                                                      final Boolean ordered) {
        return PushSubscribeOptions.builder()
                .stream(stream)
                .name(consumer)
                .bind(true)
                .ordered(ordered)
                .build();
    }

    private PullSubscribeOptions pullSubscribeOptions(final String stream, final String consumer) {
        return PullSubscribeOptions.builder()
                .stream(stream)
                .name(consumer)
                .bind(true)
                .build();
    }
}
