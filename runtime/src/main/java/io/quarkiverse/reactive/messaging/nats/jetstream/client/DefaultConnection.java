package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.nats.client.Connection.Status.CONNECTED;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.nats.client.*;
import io.nats.client.impl.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.ResolvedMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.AttachContextTraceSupplier;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerType;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.ConsumerMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.StreamStateMapper;
import io.quarkus.tls.TlsConfigurationRegistry;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
class DefaultConnection<T> implements Connection<T> {
    private final io.nats.client.Connection connection;
    private final List<ConnectionListener> listeners;
    private final StreamStateMapper streamStateMapper;
    private final ConsumerMapper consumerMapper;
    private final MessageMapper messageMapper;
    private final PayloadMapper payloadMapper;
    private final TracerFactory tracerFactory;
    private final Vertx vertx;
    private final ConcurrentHashMap<String, Subscription<T>> subscriptions;

    DefaultConnection(final ConnectionConfiguration configuration,
            final List<ConnectionListener> listeners,
            final MessageMapper messageMapper,
            final PayloadMapper payloadMapper,
            final ConsumerMapper consumerMapper,
            final StreamStateMapper streamStateMapper,
            final TracerFactory tracerFactory,
            final Vertx vertx,
            final TlsConfigurationRegistry tlsConfigurationRegistry) throws ConnectionException {
        this.connection = connect(configuration, tlsConfigurationRegistry);
        this.listeners = listeners;
        this.streamStateMapper = streamStateMapper;
        this.consumerMapper = consumerMapper;
        this.messageMapper = messageMapper;
        this.payloadMapper = payloadMapper;
        this.tracerFactory = tracerFactory;
        this.vertx = vertx;
        this.subscriptions = new ConcurrentHashMap<>();
        listeners.forEach(listener -> listener.onEvent(ConnectionEvent.Connected, "Connection established"));
    }

    @Override
    public boolean isConnected() {
        return CONNECTED.equals(connection.getStatus());
    }

    @Override
    public List<ConnectionListener> listeners() {
        return listeners;
    }

    @Override
    public void close() {
        subscriptions.forEach((subject, subscription) -> {
            try {
                subscription.close();
            } catch (Exception failure) {
                log.warnf(failure, "Error closing subscription to subject: %s with message: %s", subject, failure.getMessage());
            }
        });
        try {
            connection.close();
        } catch (Throwable throwable) {
            log.warnf(throwable, "Error closing connection: %s", throwable.getMessage());
        }
    }

    @Override
    public Uni<Message<T>> publish(final Message<T> message, final PublishConfiguration configuration) {
        return context().executeBlocking(addPublishMetadata(message, configuration)
                .onItem().transformToUni(msg -> tracerFactory.<T> create(TracerType.Publish).withTrace(msg, m -> m))
                .onItem().transformToUni(this::publishMessage)
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(failure -> notAcknowledge(message, failure))
                .onFailure().transform(failure -> new PublishException(failure.getMessage(), failure)));
    }

    @Override
    public Uni<Message<T>> publish(Message<T> message, PublishConfiguration publishConfiguration,
            ConsumerConfiguration<T> consumerConfiguration) {
        return addConsumer(consumerConfiguration)
                .onItem().invoke(consumer -> log.infof("Consumer created: %s", consumer))
                .onItem().transformToUni(ignore -> publish(message, publishConfiguration));
    }

    @Override
    public Uni<Consumer> addConsumer(ConsumerConfiguration<T> configuration) {
        return context().executeBlocking(addOrUpdateConsumer(configuration)
                .onItem()
                .transform(Unchecked.function(consumerContext -> consumerMapper.of(consumerContext.getConsumerInfo()))))
                .onFailure().transform(SystemException::new);
    }

    @Override
    public Uni<Message<T>> next(ConsumerConfiguration<T> configuration, Duration timeout) {
        final var context = context();
        return context.executeBlocking(addOrUpdateConsumer(configuration)
                .onItem()
                .transformToUni(
                        consumerContext -> Uni.createFrom().item(Unchecked.supplier(() -> consumerContext.next(timeout))))
                .emitOn(context::runOnContext)
                .onItem().ifNull().failWith(MessageNotFoundException::new)
                .onItem().ifNotNull().transformToUni(message -> transformMessage(message, configuration, context()))
                .onItem().transformToUni(message -> tracerFactory.<T> create(TracerType.Subscribe).withTrace(message,
                        new AttachContextTraceSupplier<>())))
                .onFailure().transform(failure -> {
                    if (failure instanceof MessageNotFoundException) {
                        return failure;
                    }
                    return new FetchException(failure);
                });
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public Multi<Message<T>> fetch(FetchConsumerConfiguration<T> configuration) {
        final var context = context();
        return addOrUpdateConsumer(configuration.consumerConfiguration())
                .onItem().transformToMulti(consumerContext -> fetchMessages(consumerContext, configuration, context))
                .onItem().transformToUniAndMerge(message -> tracerFactory.<T> create(TracerType.Subscribe).withTrace(message,
                        new AttachContextTraceSupplier<>()))
                .onFailure().transform(FetchException::new);
    }

    @Override
    public Uni<Message<T>> resolve(String streamName, long sequence) {
        return context().executeBlocking(Uni.createFrom().<Message<T>> item(Unchecked.supplier(() -> {
            final var jetStream = connection.jetStream();
            final var streamContext = jetStream.getStreamContext(streamName);
            final var messageInfo = streamContext.getMessage(sequence);
            return new ResolvedMessage<>(messageInfo, payloadMapper.<T> of(messageInfo).orElse(null));
        })))
                .onFailure().transform(ResolveException::new);
    }

    @Override
    public Uni<Subscription<T>> subscribe(PushConsumerConfiguration<T> configuration) {
        final var context = context();
        return context.executeBlocking(Uni.createFrom().<Subscription<T>> item(Unchecked.supplier(() -> {
            final var subscription = new PushSubscription<>(connection, configuration, messageMapper, tracerFactory, context);
            subscriptions.put(configuration.subject(), subscription);
            return subscription;
        })))
                .onFailure().transform(SubscribeException::new);
    }

    @Override
    public Uni<Subscription<T>> subscribe(PullConsumerConfiguration<T> configuration) {
        final var context = context();
        return context.executeBlocking(addOrUpdateConsumer(configuration.consumerConfiguration())
                .onItem()
                .transform(consumerContext -> new PullSubscription<>(configuration, consumerContext, messageMapper,
                        tracerFactory, context)))
                .onItem().<Subscription<T>> transform(subscription -> {
                    subscriptions.put(configuration.consumerConfiguration().subject(), subscription);
                    return subscription;
                })
                .onFailure().transform(SubscribeException::new);
    }

    @Override
    public Uni<KeyValueStore<T>> keyValueStore(final String bucketName) {
        return context().executeBlocking(
                Uni.createFrom().item(() -> new DefaultKeyValueStore<>(bucketName, connection, payloadMapper, vertx)));
    }

    @Override
    public Uni<StreamManagement> streamManagement() {
        return context().executeBlocking(
                Uni.createFrom().item(() -> new DefaultStreamManagement(connection, streamStateMapper, consumerMapper, vertx)));
    }

    @Override
    public Uni<KeyValueStoreManagement> keyValueStoreManagement() {
        return context().executeBlocking(Uni.createFrom().item(() -> new DefaultKeyValueStoreManagement(connection, vertx)));
    }

    private PublishOptions createPublishOptions(final String messageId, final String streamName) {
        return PublishOptions.builder()
                .messageId(messageId)
                .stream(streamName)
                .build();
    }

    private Uni<Message<T>> acknowledge(final Message<T> message) {
        return Uni.createFrom().completionStage(message.ack())
                .onItem().transform(v -> message);
    }

    private Uni<Message<T>> notAcknowledge(final Message<T> message, final Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(throwable))
                .onItem().invoke(() -> log.warnf(throwable, "Message not acknowledged: %s", throwable.getMessage()))
                .onItem().transformToUni(v -> Uni.createFrom().item(message));
    }

    private Headers toJetStreamHeaders(Map<String, List<String>> headers) {
        final var result = new Headers();
        headers.forEach(result::add);
        return result;
    }

    private Uni<Message<T>> publishMessage(final Message<T> message) {
        return getJetStream()
                .onItem().transformToUni(jetStream -> getMetadata(message).onItem()
                        .transformToUni(metadata -> Uni.createFrom().item(Unchecked.supplier(() -> jetStream.publish(
                                metadata.subject(),
                                toJetStreamHeaders(metadata.headers()),
                                metadata.payload(),
                                createPublishOptions(metadata.messageId(), metadata.stream())))))
                        .onItem().invoke(ack -> log.infof("Message published : %s", ack))
                        .onItem().transform(ignore -> message));
    }

    private Uni<PublishMessageMetadata> getMetadata(final Message<T> message) {
        return Uni.createFrom().item(() -> message.getMetadata(PublishMessageMetadata.class).orElse(null))
                .onItem().ifNull().failWith(() -> new RuntimeException("Metadata not found"));
    }

    private Uni<JetStream> getJetStream() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStream));
    }

    private Uni<Message<T>> addPublishMetadata(final Message<T> message, final PublishConfiguration configuration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            final var publishMetadata = PublishMessageMetadata.of(message, configuration,
                    payloadMapper.of(message.getPayload()));
            final var metadata = message.getMetadata().without(PublishMessageMetadata.class);
            return message.withMetadata(metadata.with(publishMetadata));
        }));
    }

    private Multi<Message<T>> fetchMessages(ConsumerContext consumerContext, FetchConsumerConfiguration<T> configuration,
            Context context) {
        ExecutorService executor = Executors.newSingleThreadExecutor(JetstreamWorkerThread::new);
        return Multi.createFrom().<io.nats.client.Message> emitter(emitter -> {
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
                emitter.fail(new FetchException(failure));
            }
        })
                .runSubscriptionOn(executor)
                .emitOn(context::runOnContext)
                .onItem()
                .transformToUniAndMerge(message -> transformMessage(message, configuration.consumerConfiguration(), context));
    }

    private FetchConsumer fetchConsumer(final ConsumerContext consumerContext,
            final FetchConsumerConfiguration<T> configuration)
            throws IOException, JetStreamApiException {
        if (configuration.timeout() == null) {
            return consumerContext.fetch(FetchConsumeOptions.builder().maxMessages(configuration.batchSize()).noWait().build());
        } else {
            return consumerContext.fetch(FetchConsumeOptions.builder().maxMessages(configuration.batchSize())
                    .expiresIn(configuration.timeout().toMillis()).build());
        }
    }

    private Uni<Message<T>> transformMessage(io.nats.client.Message message, ConsumerConfiguration<T> configuration,
            Context context) {
        return Uni.createFrom()
                .item(Unchecked.supplier(() -> messageMapper.of(message, configuration.payloadType().orElse(null), context,
                        configuration.acknowledgeTimeout().orElse(null))));
    }

    private Uni<ConsumerContext> addOrUpdateConsumer(ConsumerConfiguration<T> configuration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var factory = new ConsumerConfigurationFactory();
                final var consumerConfiguration = factory.create(configuration);
                final var streamContext = connection.getStreamContext(configuration.stream());
                return streamContext.createOrUpdateConsumer(consumerConfiguration);
            } catch (Exception failure) {
                throw new SystemException(failure);
            }
        }));
    }

    private Context context() {
        return vertx.getOrCreateContext();
    }

    private io.nats.client.Connection connect(ConnectionConfiguration configuration,
            TlsConfigurationRegistry tlsConfigurationRegistry) throws ConnectionException {
        try {
            final var factory = new ConnectionOptionsFactory();
            final var options = factory.create(configuration, new InternalConnectionListener<>(this), tlsConfigurationRegistry);
            return Nats.connect(options);
        } catch (Exception failure) {
            throw new ConnectionException(failure);
        }
    }
}
