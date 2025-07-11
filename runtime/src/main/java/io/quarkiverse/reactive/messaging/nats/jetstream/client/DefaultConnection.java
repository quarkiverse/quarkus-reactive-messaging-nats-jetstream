package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.nats.client.Connection.Status.CONNECTED;
import static io.nats.client.Options.DEFAULT_RECONNECT_WAIT;
import static io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessage.DEFAULT_ACK_TIMEOUT;

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
import io.quarkus.tls.TlsConfiguration;
import io.quarkus.tls.TlsConfigurationRegistry;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
class DefaultConnection extends AbstractConsumer implements Connection {
    private final io.nats.client.Connection connection;
    private final List<ConnectionListener> listeners;
    private final StreamStateMapper streamStateMapper;
    private final ConsumerMapper consumerMapper;
    private final MessageMapper messageMapper;
    private final PayloadMapper payloadMapper;
    private final TracerFactory tracerFactory;
    private final Vertx vertx;
    private final ConcurrentHashMap<String, Subscription<?>> subscriptions;

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
        subscriptions.forEach((consumer, subscription) -> {
            try {
                subscription.close();
            } catch (Exception failure) {
                log.warnf(failure, "Error closing subscription to consumer: %s with message: %s", consumer,
                        failure.getMessage());
            }
        });
        try {
            connection.close();
        } catch (Throwable throwable) {
            log.warnf(throwable, "Error closing connection: %s", throwable.getMessage());
        }
    }

    @Override
    public <T> Uni<Message<T>> publish(final Message<T> message, final String stream, final String subject) {
        return context().executeBlocking(publishInternal(message, stream, subject));
    }

    @Override
    public Uni<Consumer> addConsumer(final String stream, String name, final ConsumerConfiguration configuration) {
        return context().executeBlocking(addOrUpdateConsumer(stream, name, configuration)
                .onItem()
                .transform(Unchecked.function(consumerContext -> consumerMapper.of(consumerContext.getConsumerInfo()))))
                .onFailure().transform(SystemException::new);
    }

    @Override
    public <T> Uni<Message<T>> next(final String stream, final String consumer, ConsumerConfiguration configuration,
            final Duration timeout) {
        final var context = context();
        return context.executeBlocking(getConsumerContext(stream, consumer)
                .onItem()
                .transformToUni(consumerContext -> next(consumerContext, configuration, timeout)));
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public <T> Multi<Message<T>> fetch(final String stream, final String consumer,
            final FetchConsumerConfiguration configuration) {
        final var context = context();
        return getConsumerContext(stream, consumer)
                .onItem()
                .<Message<T>> transformToMulti(consumerContext -> fetchMessages(consumerContext, configuration, context))
                .onItem().transformToUniAndMerge(message -> tracerFactory.<T> create(TracerType.Subscribe).withTrace(message,
                        new AttachContextTraceSupplier<>()))
                .onFailure().transform(FetchException::new);
    }

    @Override
    public <T> Uni<Message<T>> resolve(String streamName, long sequence) {
        return context().executeBlocking(Uni.createFrom().<Message<T>> item(Unchecked.supplier(() -> {
            final var jetStream = connection.jetStream();
            final var streamContext = jetStream.getStreamContext(streamName);
            final var messageInfo = streamContext.getMessage(sequence);
            return new ResolvedMessage<>(messageInfo, payloadMapper.<T> of(messageInfo).orElse(null));
        })))
                .onFailure().transform(ResolveException::new);
    }

    @Override
    public <T> Uni<Subscription<T>> subscribe(final String stream, final String consumer,
            final PushConsumerConfiguration configuration) {
        final var context = context();
        return context.executeBlocking(Uni.createFrom().<Subscription<T>> item(Unchecked.supplier(() -> {
            final var subscription = new PushSubscription<T>(connection, stream, consumer, configuration, messageMapper,
                    tracerFactory,
                    context);
            subscriptions.put(consumer, subscription);
            return subscription;
        })))
                .onFailure().transform(SubscribeException::new);
    }

    @Override
    public <T> Uni<Subscription<T>> subscribe(final String stream, final String consumer,
            final PullConsumerConfiguration configuration) {
        final var context = context();
        return context.executeBlocking(addOrUpdateConsumer(stream, consumer, configuration.consumerConfiguration()))
                .onItem()
                .<Subscription<T>> transformToUni(
                        consumerContext -> createPullSubscription(stream, consumer, consumerContext, configuration))
                .onItem().transform(subscription -> {
                    subscriptions.put(consumer, subscription);
                    return subscription;
                })
                .onFailure().transform(SubscribeException::new);
    }

    @Override
    public Uni<KeyValueStore> keyValueStore(final String bucketName) {
        return context().executeBlocking(
                Uni.createFrom().item(() -> new DefaultKeyValueStore(bucketName, connection, payloadMapper, vertx)));
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

    @Override
    public void nativeConnection(java.util.function.Consumer<io.nats.client.Connection> connection) {
        connection.accept(this.connection);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Request, Reply> Uni<Message<Reply>> request(Message<Request> message,
            RequestReplyConsumerConfiguration configuration) {
        return context().executeBlocking(
                addOrUpdateConsumer(configuration.stream(), configuration.name(), configuration.consumerConfiguration())
                        .onItem()
                        .transformToUni(consumerContext -> publishInternal(message, configuration.stream(),
                                configuration.requestSubject())
                                .onItem().transform(ignore -> consumerContext))
                        .onItem()
                        .transformToUni(consumerContext -> next(consumerContext, configuration.consumerConfiguration(),
                                configuration.timeout()))
                        .onItem().transform(reply -> (Message<Reply>) reply));
    }

    private PublishOptions createPublishOptions(final String messageId, final String streamName) {
        return PublishOptions.builder()
                .messageId(messageId)
                .stream(streamName)
                .build();
    }

    private <T> Uni<Message<T>> acknowledge(final Message<T> message) {
        return Uni.createFrom().completionStage(message.ack())
                .onItem().transform(v -> message);
    }

    private <T> Uni<Message<T>> notAcknowledge(final Message<T> message, final Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(throwable))
                .onItem().invoke(() -> log.warnf(throwable, "Message not acknowledged: %s", throwable.getMessage()))
                .onItem().transformToUni(v -> Uni.createFrom().item(message));
    }

    private Headers toJetStreamHeaders(Map<String, List<String>> headers) {
        final var result = new Headers();
        headers.forEach(result::add);
        return result;
    }

    private <T> Uni<Message<T>> publishMessage(final Message<T> message) {
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

    private <T> Uni<PublishMessageMetadata> getMetadata(final Message<T> message) {
        return Uni.createFrom().item(() -> message.getMetadata(PublishMessageMetadata.class).orElse(null))
                .onItem().ifNull().failWith(() -> new RuntimeException("Metadata not found"));
    }

    private Uni<JetStream> getJetStream() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStream));
    }

    private <T> Uni<Message<T>> addPublishMetadata(final Message<T> message, final String stream,
            final String subject) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            final var publishMetadata = PublishMessageMetadata.of(message, stream, subject,
                    payloadMapper.of(message.getPayload()));
            final var metadata = message.getMetadata().without(PublishMessageMetadata.class);
            return message.withMetadata(metadata.with(publishMetadata));
        }));
    }

    private <T> Multi<Message<T>> fetchMessages(ConsumerContext consumerContext, FetchConsumerConfiguration configuration,
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
            final FetchConsumerConfiguration configuration)
            throws IOException, JetStreamApiException {
        if (configuration.fetchConfiguration().timeout().isEmpty()) {
            return consumerContext.fetch(
                    FetchConsumeOptions.builder().maxMessages(configuration.fetchConfiguration().batchSize()).noWait().build());
        } else {
            return consumerContext
                    .fetch(FetchConsumeOptions.builder().maxMessages(configuration.fetchConfiguration().batchSize())
                            .expiresIn(configuration.fetchConfiguration().timeout().get().toMillis()).build());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Uni<Message<T>> transformMessage(io.nats.client.Message message,
            ConsumerConfiguration configuration,
            Context context) {
        Class<T> payloadType = (Class<T>) configuration.payloadType().orElse(null);
        return Uni.createFrom()
                .item(Unchecked.<Message<T>> supplier(
                        () -> messageMapper.of(message, payloadType, context,
                                configuration.acknowledgeTimeout().orElse(DEFAULT_ACK_TIMEOUT),
                                configuration.backoff().orElseGet(List::of))));
    }

    private Uni<ConsumerContext> addOrUpdateConsumer(final String stream, final String name,
            final ConsumerConfiguration configuration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var consumerConfiguration = createConsumerConfiguration(name, configuration);
                final var streamContext = connection.getStreamContext(stream);
                return streamContext.createOrUpdateConsumer(consumerConfiguration);
            } catch (Exception failure) {
                throw new SystemException(failure);
            }
        }));
    }

    private Uni<ConsumerContext> getConsumerContext(final String stream, final String consumer) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var streamContext = connection.getStreamContext(stream);
                return streamContext.getConsumerContext(consumer);
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
            final var options = createConnectionOptions(configuration, new InternalConnectionListener(this),
                    tlsConfigurationRegistry);
            return Nats.connect(options);
        } catch (Exception failure) {
            throw new ConnectionException(failure);
        }
    }

    public Options createConnectionOptions(final ConnectionConfiguration configuration,
            final io.nats.client.ConnectionListener connectionListener,
            final TlsConfigurationRegistry tlsConfigurationRegistry)
            throws Exception {
        final var optionsBuilder = new Options.Builder();
        final var servers = configuration.servers();
        optionsBuilder.servers(servers.toArray(new String[0]));
        optionsBuilder.maxReconnects(configuration.connectionAttempts());
        optionsBuilder.connectionTimeout(configuration.connectionBackoff().orElse(DEFAULT_RECONNECT_WAIT));
        if (connectionListener != null) {
            optionsBuilder.connectionListener(connectionListener);
        }
        optionsBuilder.errorListener(getErrorListener(configuration));
        configuration.username()
                .ifPresent(username -> optionsBuilder.userInfo(username, configuration.password().orElse("")));
        configuration.token().map(String::toCharArray).ifPresent(optionsBuilder::token);
        configuration.credentialPath().ifPresent(optionsBuilder::credentialPath);
        configuration.bufferSize().ifPresent(optionsBuilder::bufferSize);
        configuration.connectionTimeout().ifPresent(optionsBuilder::connectionTimeout);
        if (configuration.sslEnabled().orElse(false)) {
            optionsBuilder.opentls();
            final var tlsConfiguration = configuration.tlsConfigurationName()
                    .flatMap(tlsConfigurationRegistry::get)
                    .orElseGet(() -> getDefaultTlsConfiguration(tlsConfigurationRegistry));
            optionsBuilder.sslContext(tlsConfiguration.createSSLContext());
        }
        configuration.tlsAlgorithm().ifPresent(optionsBuilder::tlsAlgorithm);
        return optionsBuilder.build();
    }

    private ErrorListener getErrorListener(ConnectionConfiguration configuration) {
        return configuration.errorListener()
                .orElseGet(DefaultErrorListener::new);
    }

    private TlsConfiguration getDefaultTlsConfiguration(TlsConfigurationRegistry tlsConfigurationRegistry) {
        return tlsConfigurationRegistry.getDefault().orElseThrow(
                () -> new IllegalStateException("No Quarkus TLS configuration found for NATS JetStream connection"));
    }

    private <T> Uni<Message<T>> next(final ConsumerContext consumerContext, ConsumerConfiguration configuration,
            final Duration timeout) {
        return Uni.createFrom().item(Unchecked.supplier(() -> consumerContext.next(timeout)))
                .onItem().ifNull().failWith(MessageNotFoundException::new)
                .onItem().ifNotNull()
                .<Message<T>> transformToUni(message -> transformMessage(message, configuration, context()))
                .onItem().transformToUni(message -> tracerFactory.<T> create(TracerType.Subscribe).withTrace(message,
                        new AttachContextTraceSupplier<>()))
                .onFailure().transform(failure -> {
                    if (failure instanceof MessageNotFoundException) {
                        return failure;
                    }
                    return new FetchException(failure);
                });
    }

    private <T> Uni<Message<T>> publishInternal(final Message<T> message, final String stream, final String subject) {
        return addPublishMetadata(message, stream, subject)
                .onItem().transformToUni(msg -> tracerFactory.<T> create(TracerType.Publish).withTrace(msg, m -> m))
                .onItem().transformToUni(this::publishMessage)
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(failure -> notAcknowledge(message, failure))
                .onFailure().transform(failure -> new PublishException(failure.getMessage(), failure));
    }

    private <T> Uni<Subscription<T>> createPullSubscription(
            final String stream, final String consumer, ConsumerContext consumerContext,
            final PullConsumerConfiguration configuration) {
        if (configuration.pullConfiguration().batchSize() <= 1) {
            return Uni.createFrom().item(Unchecked.supplier(
                    () -> new PullSubscription<>(
                            new PullMessageConsumer(stream, configuration, consumerContext),
                            configuration, messageMapper, tracerFactory, context())));
        } else {
            return Uni.createFrom().item(Unchecked.supplier(
                    () -> new PullSubscription<>(
                            new PullMessageReader(connection.jetStream(), stream, consumer, configuration),
                            configuration, messageMapper, tracerFactory, context())));
        }
    }
}
