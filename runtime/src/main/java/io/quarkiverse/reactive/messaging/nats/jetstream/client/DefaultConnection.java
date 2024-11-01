package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.nats.client.Connection.Status.CONNECTED;
import static io.quarkiverse.reactive.messaging.nats.jetstream.mapper.DefaultMessageMapper.MESSAGE_TYPE_HEADER;
import static io.smallrye.reactive.messaging.tracing.TracingUtils.traceOutgoing;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.*;
import io.nats.client.api.ConsumerInfo;
import io.nats.client.api.KeyValueEntry;
import io.nats.client.api.StreamInfo;
import io.nats.client.api.StreamInfoOptions;
import io.nats.client.impl.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamOutgoingMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.ConsumerMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.StreamStateMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrument;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamTrace;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.tuples.Tuple5;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;

public class DefaultConnection implements Connection {
    private final static Logger logger = Logger.getLogger(DefaultConnection.class);

    private final io.nats.client.Connection connection;
    private final List<ConnectionListener> listeners;
    private final Context context;
    private final StreamStateMapper streamStateMapper;
    private final ConsumerMapper consumerMapper;
    private final MessageMapper messageMapper;
    private final PayloadMapper payloadMapper;
    private final JetStreamInstrument instrument;

    DefaultConnection(final ConnectionConfiguration configuration,
            final ConnectionListener connectionListener,
            final Context context,
            final MessageMapper messageMapper,
            final PayloadMapper payloadMapper,
            final ConsumerMapper consumerMapper,
            final StreamStateMapper streamStateMapper,
            final JetStreamInstrument instrumenter) throws ConnectionException {
        this.connection = connect(configuration);
        this.listeners = new ArrayList<>(List.of(connectionListener));
        this.context = context;
        this.streamStateMapper = streamStateMapper;
        this.consumerMapper = consumerMapper;
        this.messageMapper = messageMapper;
        this.payloadMapper = payloadMapper;
        this.instrument = instrumenter;
        fireEvent(ConnectionEvent.Connected, "Connection established");
    }

    @Override
    public boolean isConnected() {
        return CONNECTED.equals(connection.getStatus());
    }

    @Override
    public Uni<Void> flush(Duration duration) {
        return Uni.createFrom().<Void> item(Unchecked.supplier(() -> {
            try {
                connection.flush(duration);
                return null;
            } catch (TimeoutException | InterruptedException e) {
                throw new ConnectionException(e);
            }
        }))
                .emitOn(context::runOnContext);
    }

    @Override
    public List<ConnectionListener> listeners() {
        return listeners;
    }

    @Override
    public void addListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ConnectionListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void close() {
        new ArrayList<>(listeners).forEach(listener -> {
            try {
                listener.close();
            } catch (Throwable failure) {
                logger.warnf(failure, "Error closing listener: %s", failure.getMessage());
            }
        });
        try {
            connection.close();
        } catch (Throwable throwable) {
            logger.warnf(throwable, "Error closing connection: %s", throwable.getMessage());
        }
    }

    @Override
    public Uni<Consumer> getConsumer(String stream, String consumerName) {
        return getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().<ConsumerInfo> emitter(emitter -> {
                    try {
                        emitter.complete(jsm.getConsumerInfo(stream, consumerName));
                    } catch (IOException | JetStreamApiException e) {
                        emitter.fail(new SystemException(e));
                    }
                }))
                .onItem().transform(consumerMapper::of)
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Void> deleteConsumer(String streamName, String consumerName) {
        return getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().<Void> emitter(emitter -> {
                    try {
                        jsm.deleteConsumer(streamName, consumerName);
                        emitter.complete(null);
                    } catch (Throwable failure) {
                        emitter.fail(new SystemException(failure));
                    }
                }))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<List<String>> getStreams() {
        return getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().item(Unchecked.supplier((jsm::getStreamNames))))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<List<String>> getSubjects(String streamName) {
        return getStreamInfo(streamName)
                .onItem().transform(stream -> stream.getConfiguration().getSubjects())
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<List<String>> getConsumerNames(String streamName) {
        return getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().<List<String>> emitter(emitter -> {
                    try {
                        emitter.complete(jsm.getConsumerNames(streamName));
                    } catch (Throwable failure) {
                        emitter.fail(new SystemException(failure));
                    }
                }))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<PurgeResult> purgeStream(String streamName) {
        return getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().<PurgeResult> emitter(emitter -> {
                    try {
                        final var response = jsm.purgeStream(streamName);
                        emitter.complete(new PurgeResult(streamName, response.isSuccess(), response.getPurged()));
                    } catch (Throwable failure) {
                        emitter.fail(new SystemException(failure));
                    }
                }))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Void> deleteMessage(String stream, long sequence, boolean erase) {
        return getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().<Void> emitter(emitter -> {
                    try {
                        if (!jsm.deleteMessage(stream, sequence, erase)) {
                            emitter.fail(new DeleteException(
                                    String.format("Unable to delete message in stream %s with sequence %d", stream, sequence)));
                        }
                        emitter.complete(null);
                    } catch (Throwable failure) {
                        emitter.fail(new DeleteException(
                                String.format("Unable to delete message in stream %s with sequence %d: %s", stream,
                                        sequence, failure.getMessage()),
                                failure));
                    }
                }))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<StreamState> getStreamState(String streamName) {
        return getStreamInfo(streamName)
                .onItem().transform(streamInfo -> streamStateMapper.of(streamInfo.getStreamState()))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<StreamConfiguration> getStreamConfiguration(String streamName) {
        return getStreamInfo(streamName)
                .onItem().transform(streamInfo -> StreamConfiguration.of(streamInfo.getConfiguration()))
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<List<PurgeResult>> purgeAllStreams() {
        return getStreams()
                .onItem().transformToUni(this::purgeAllStreams)
                .emitOn(context::runOnContext);
    }

    @Override
    public <T> Uni<Message<T>> publish(final Message<T> message, final PublishConfiguration configuration) {
        return Uni.createFrom().<Tuple5<JetStream, String, Headers, byte[], PublishOptions>> emitter(emitter -> {
            try {
                final var metadata = message.getMetadata(JetStreamOutgoingMessageMetadata.class);
                final var messageId = metadata.map(JetStreamOutgoingMessageMetadata::messageId)
                        .orElseGet(() -> UUID.randomUUID().toString());
                final var payload = payloadMapper.of(message.getPayload());
                final var subject = metadata.flatMap(JetStreamOutgoingMessageMetadata::subtopic)
                        .map(subtopic -> configuration.subject() + "." + subtopic).orElseGet(configuration::subject);
                final var headers = new HashMap<String, List<String>>();
                metadata.ifPresent(m -> headers.putAll(m.headers()));
                if (message.getPayload() != null) {
                    headers.putIfAbsent(MESSAGE_TYPE_HEADER, List.of(message.getPayload().getClass().getTypeName()));
                }

                if (configuration.traceEnabled()) {
                    // Create a new span for the outbound message and record updated tracing information in
                    // the headers; this has to be done before we build the properties below
                    traceOutgoing(instrument.publisher(), message,
                            new JetStreamTrace(configuration.stream(), subject, messageId, headers,
                                    new String(payload)));
                }

                final var jetStream = connection.jetStream();
                final var options = createPublishOptions(messageId, configuration.stream());

                emitter.complete(Tuple5.of(jetStream, subject, toJetStreamHeaders(headers), payload, options));
            } catch (Throwable failure) {
                emitter.fail(
                        new PublishException(String.format("Failed to publish message: %s", failure.getMessage()), failure));
            }
        })
                .onItem()
                .transformToUni(tuple -> Uni.createFrom().completionStage(
                        tuple.getItem1().publishAsync(tuple.getItem2(), tuple.getItem3(), tuple.getItem4(), tuple.getItem5())))
                .onItem()
                .invoke(ack -> logger.debugf("Message published to stream: %s with sequence number: %d", ack.getStream(),
                        ack.getSeqno()))
                .onItem().transformToUni(ignore -> acknowledge(message))
                .onFailure().recoverWithUni(failure -> notAcknowledge(message, failure))
                .onFailure().transform(failure -> new PublishException(failure.getMessage(), failure))
                .emitOn(context::runOnContext);
    }

    @Override
    public <T> Uni<Message<T>> publish(Message<T> message,
            PublishConfiguration publishConfiguration,
            FetchConsumerConfiguration<T> consumerConfiguration) {
        return addOrUpdateConsumer(consumerConfiguration)
                .onItem().transformToUni(v -> publish(message, publishConfiguration));
    }

    @Override
    public <T> Uni<Message<T>> nextMessage(FetchConsumerConfiguration<T> configuration) {
        return getConsumerContext(configuration)
                .onItem()
                .transformToUni(consumerContext -> nextMessage(consumerContext, configuration));
    }

    @Override
    public <T> Multi<Message<T>> nextMessages(FetchConsumerConfiguration<T> configuration) {
        return getConsumerContext(configuration)
                .onItem().transformToMulti(consumerContext -> nextMessages(consumerContext, configuration))
                .emitOn(context::runOnContext);
    }

    @Override
    public <T> Uni<T> getKeyValue(String bucketName, String key, Class<T> valueType) {
        return Uni.createFrom().<KeyValueEntry> emitter(emitter -> {
            try {
                final var keyValue = connection.keyValue(bucketName);
                emitter.complete(keyValue.get(key));
            } catch (Throwable failure) {
                emitter.fail(new KeyValueException(failure));
            }
        })
                .onItem().ifNull().failWith(() -> new KeyValueNotFoundException(bucketName, key))
                .onItem().ifNotNull().transform(keyValueEntry -> payloadMapper.of(keyValueEntry.getValue(), valueType))
                .emitOn(context::runOnContext);
    }

    @Override
    public <T> Uni<Void> putKeyValue(String bucketName, String key, T value) {
        return Uni.createFrom().<Void> emitter(emitter -> {
            try {
                KeyValue keyValue = connection.keyValue(bucketName);
                keyValue.put(key, payloadMapper.of(value));
                emitter.complete(null);
            } catch (Throwable failure) {
                emitter.fail(new KeyValueException(failure));
            }
        })
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Void> deleteKeyValue(String bucketName, String key) {
        return Uni.createFrom().<Void> emitter(emitter -> {
            try {
                KeyValue keyValue = connection.keyValue(bucketName);
                keyValue.delete(key);
                emitter.complete(null);
            } catch (Throwable failure) {
                emitter.fail(new KeyValueException(failure));
            }
        })
                .emitOn(context::runOnContext);
    }

    @Override
    public <T> Uni<Message<T>> resolve(String streamName, long sequence) {
        return Uni.createFrom().<Message<T>> emitter(emitter -> {
            try {
                final var jetStream = connection.jetStream();
                final var streamContext = jetStream.getStreamContext(streamName);
                final var messageInfo = streamContext.getMessage(sequence);
                emitter.complete(new JetStreamMessage<>(messageInfo, payloadMapper.<T> of(messageInfo).orElse(null)));
            } catch (IOException | JetStreamApiException e) {
                emitter.fail(e);
            }
        })
                .emitOn(context::runOnContext);
    }

    @Override
    public <T> Uni<Subscription<T>> subscribtion(PushConsumerConfiguration<T> configuration) {
        return Uni.createFrom().item(() -> new PushSubscription<>(this, configuration, connection, messageMapper, context));
    }

    @Override
    public <T> Uni<Subscription<T>> subscribtion(ReaderConsumerConfiguration<T> configuration) {
        return createSubscription(configuration)
                .onItem().transformToUni(subscription -> createReader(configuration, subscription))
                .onItem()
                .transformToUni(pair -> Uni.createFrom()
                        .<Subscription<T>> item(Unchecked.supplier(() -> new ReaderSubscribtion<>(this, configuration,
                                pair.getItem1(), pair.getItem2(), messageMapper, context))))
                .onItem().invoke(this::addListener);
    }

    @Override
    public <T> void close(Subscription<T> subscription) {
        try {
            subscription.close();
        } catch (Throwable failure) {
            logger.warnf(failure, "Failed to close subscription: %s", failure.getMessage());
        }
        removeListener(subscription);
    }

    @Override
    public Uni<Void> addOrUpdateKeyValueStores(List<KeyValueSetupConfiguration> keyValueConfigurations) {
        return Multi.createFrom().items(keyValueConfigurations.stream())
                .onItem().transformToUniAndMerge(this::addOrUpdateKeyValueStore)
                .collect().last()
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<List<StreamResult>> addStreams(List<StreamSetupConfiguration> streamConfigurations) {
        return getJetStreamManagement()
                .onItem()
                .transformToMulti(jetStreamManagement -> Multi.createFrom()
                        .items(streamConfigurations.stream()
                                .map(streamConfiguration -> Tuple2.of(jetStreamManagement, streamConfiguration))))
                .onItem().transformToUniAndMerge(tuple -> addOrUpdateStream(tuple.getItem1(), tuple.getItem2()))
                .collect().asList()
                .emitOn(context::runOnContext);
    }

    private <T> Uni<Tuple2<JetStreamSubscription, JetStreamReader>> createReader(ReaderConsumerConfiguration<T> configuration,
            JetStreamSubscription subscription) {
        return Uni.createFrom()
                .item(Unchecked.supplier(() -> subscription.reader(configuration.maxRequestBatch(), configuration.rePullAt())))
                .onItem().transform(reader -> Tuple2.of(subscription, reader));
    }

    /**
     * Creates a subscription.
     * If an IllegalArgumentException is thrown the consumer configuration is modified.
     */
    private <T> Uni<JetStreamSubscription> createSubscription(ReaderConsumerConfiguration<T> configuration) {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStream))
                .onItem().transformToUni(jetStream -> createSubscription(jetStream, configuration));
    }

    private <T> Uni<JetStreamSubscription> createSubscription(JetStream jetStream,
            ReaderConsumerConfiguration<T> configuration) {
        return subscribe(jetStream, configuration)
                .onFailure().recoverWithUni(failure -> {
                    if (failure instanceof IllegalArgumentException) {
                        return deleteConsumer(configuration.consumerConfiguration().stream(),
                                configuration.consumerConfiguration().name())
                                .onItem().transformToUni(v -> subscribe(jetStream, configuration));
                    } else {
                        return Uni.createFrom().failure(failure);
                    }
                });
    }

    private <T> Uni<JetStreamSubscription> subscribe(JetStream jetStream, ReaderConsumerConfiguration<T> configuration) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                final var optionsFactory = new PullSubscribeOptionsFactory();
                emitter.complete(jetStream.subscribe(configuration.subject(), optionsFactory.create(configuration)));
            } catch (Throwable failure) {
                emitter.fail(failure);
            }
        });
    }

    private Uni<StreamInfo> getStreamInfo(String streamName) {
        return getJetStreamManagement()
                .onItem().transformToUni(jsm -> getStreamInfo(jsm, streamName));
    }

    private Uni<StreamInfo> getStreamInfo(JetStreamManagement jsm, String streamName) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                emitter.complete(jsm.getStreamInfo(streamName, StreamInfoOptions.allSubjects()));
            } catch (Throwable failure) {
                emitter.fail(new SystemException(
                        String.format("Unable to read stream %s with message: %s", streamName, failure.getMessage()), failure));
            }
        });
    }

    private Optional<PurgeResult> purgeStream(JetStreamManagement jetStreamManagement, String streamName) {
        try {
            final var response = jetStreamManagement.purgeStream(streamName);
            return Optional.of(PurgeResult.builder()
                    .streamName(streamName)
                    .success(response.isSuccess())
                    .purgeCount(response.getPurged()).build());
        } catch (IOException | JetStreamApiException e) {
            logger.warnf(e, "Unable to purge stream %s with message: %s", streamName, e.getMessage());
            return Optional.empty();
        }
    }

    private Uni<List<PurgeResult>> purgeAllStreams(List<String> streams) {
        return getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().item(
                        Unchecked.supplier(
                                () -> streams.stream().flatMap(streamName -> purgeStream(jsm, streamName).stream()).toList())));
    }

    private Uni<JetStreamManagement> getJetStreamManagement() {
        return Uni.createFrom().emitter(emitter -> {
            try {
                emitter.complete(connection.jetStreamManagement());
            } catch (Throwable failure) {
                emitter.fail(
                        new SystemException(String.format("Unable to manage JetStream: %s", failure.getMessage()), failure));
            }
        });
    }

    private PublishOptions createPublishOptions(final String messageId, final String streamName) {
        return PublishOptions.builder()
                .messageId(messageId)
                .stream(streamName)
                .build();
    }

    private FetchConsumer fetchConsumer(final ConsumerContext consumerContext, final Duration timeout)
            throws IOException, JetStreamApiException {
        if (timeout == null) {
            return consumerContext.fetch(FetchConsumeOptions.builder().maxMessages(1).noWait().build());
        } else {
            return consumerContext.fetch(FetchConsumeOptions.builder().maxMessages(1).expiresIn(timeout.toMillis()).build());
        }
    }

    private <T> Uni<Message<T>> acknowledge(final Message<T> message) {
        return Uni.createFrom().completionStage(message.ack())
                .onItem().transform(v -> message);
    }

    private <T> Uni<Message<T>> notAcknowledge(final Message<T> message, final Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(throwable))
                .onItem().invoke(() -> logger.warnf(throwable, "Message not acknowledged: %s", throwable.getMessage()))
                .onItem().transformToUni(v -> Uni.createFrom().item(message));
    }

    private <T> Uni<ConsumerContext> getConsumerContext(final FetchConsumerConfiguration<T> configuration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var streamContext = connection.getStreamContext(configuration.stream());
                return streamContext.getConsumerContext(configuration.name());
            } catch (JetStreamApiException e) {
                if (e.getApiErrorCode() == 10014) { // consumer not found
                    throw new ConsumerNotFoundException(configuration.stream(), configuration.name());
                } else {
                    throw new FetchException(e);
                }
            } catch (IOException e) {
                throw new FetchException(e);
            }
        }))
                .onFailure().recoverWithUni(failure -> handleConsumerContextFailure(configuration, failure))
                .emitOn(context::runOnContext);
    }

    private <T> Uni<ConsumerContext> handleConsumerContextFailure(final FetchConsumerConfiguration<T> configuration,
            Throwable failure) {
        if (failure instanceof ConsumerNotFoundException) {
            return addOrUpdateConsumer(configuration);
        } else {
            return Uni.createFrom().failure(failure);
        }
    }

    private Uni<io.nats.client.Message> nextMessage(final ConsumerContext consumerContext,
            final Duration timeout) {
        return Uni.createFrom().<io.nats.client.Message> emitter(emitter -> {
            try {
                try (final var fetchConsumer = fetchConsumer(consumerContext, timeout)) {
                    final var message = fetchConsumer.nextMessage();
                    if (message != null) {
                        emitter.complete(message);
                    } else {
                        emitter.fail(new MessageNotFoundException());
                    }
                }
            } catch (Throwable failure) {
                logger.errorf(failure, "Failed to fetch message: %s", failure.getMessage());
                emitter.fail(new FetchException(failure));
            }
        })
                .emitOn(context::runOnContext);
    }

    private <T> Uni<Message<T>> nextMessage(final ConsumerContext consumerContext,
            final FetchConsumerConfiguration<T> configuration) {
        return nextMessage(consumerContext, configuration.fetchTimeout().orElse(null))
                .map(message -> messageMapper.of(
                        message,
                        configuration.traceEnabled(),
                        configuration.payloadType().orElse(null),
                        context,
                        new ExponentialBackoff(false, Duration.ZERO),
                        configuration.ackTimeout()));
    }

    private Headers toJetStreamHeaders(Map<String, List<String>> headers) {
        final var result = new Headers();
        headers.forEach(result::add);
        return result;
    }

    private <T> Uni<ConsumerContext> addOrUpdateConsumer(ConsumerConfiguration<T> configuration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var factory = new ConsumerConfigurtationFactory();
                final var consumerConfiguration = factory.create(configuration);
                final var streamContext = connection.getStreamContext(configuration.stream());
                final var consumerContext = streamContext.createOrUpdateConsumer(consumerConfiguration);
                connection.flush(Duration.ZERO);
                return consumerContext;
            } catch (Throwable failure) {
                throw new FetchException(failure);
            }
        }))
                .emitOn(context::runOnContext);
    }

    private <T> Multi<Message<T>> nextMessages(final ConsumerContext consumerContext,
            FetchConsumerConfiguration<T> configuration) {
        return Multi.createFrom().<Message<T>> emitter(emitter -> {
            try {
                try (final var fetchConsumer = fetchConsumer(consumerContext, configuration.fetchTimeout().orElse(null))) {
                    var message = fetchConsumer.nextMessage();
                    while (message != null) {
                        emitter.emit(messageMapper.of(
                                message,
                                configuration.traceEnabled(),
                                configuration.payloadType().orElse(null),
                                context,
                                new ExponentialBackoff(false, Duration.ZERO),
                                configuration.ackTimeout()));
                        message = fetchConsumer.nextMessage();
                    }
                    emitter.complete();
                }
            } catch (Throwable failure) {
                emitter.fail(new FetchException(failure));
            }
        })
                .emitOn(context::runOnContext);
    }

    private io.nats.client.Connection connect(ConnectionConfiguration configuration) throws ConnectionException {
        try {
            ConnectionOptionsFactory optionsFactory = new ConnectionOptionsFactory();
            final var options = optionsFactory.create(configuration, new InternalConnectionListener(this));
            return Nats.connect(options);
        } catch (Throwable failure) {
            throw new ConnectionException(failure);
        }
    }

    private Uni<Void> addOrUpdateKeyValueStore(final KeyValueSetupConfiguration keyValueSetupConfiguration) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                final var kvm = connection.keyValueManagement();
                final var factory = new KeyValueConfigurationFactory();
                if (kvm.getBucketNames().contains(keyValueSetupConfiguration.bucketName())) {
                    kvm.update(factory.create(keyValueSetupConfiguration));
                } else {
                    kvm.create(factory.create(keyValueSetupConfiguration));
                }
                emitter.complete(null);
            } catch (Throwable failure) {
                emitter.fail(new SetupException(String.format("Unable to manage Key Value Store: %s", failure.getMessage()),
                        failure));
            }
        });
    }

    private Uni<StreamResult> addOrUpdateStream(final JetStreamManagement jsm,
            final StreamSetupConfiguration setupConfiguration) {
        return getStreamInfo(jsm, setupConfiguration.configuration().name())
                .onItem().transformToUni(streamInfo -> updateStream(jsm, streamInfo, setupConfiguration))
                .onFailure().recoverWithUni(failure -> createStream(jsm, setupConfiguration.configuration()));
    }

    private Uni<StreamResult> createStream(final JetStreamManagement jsm,
            final StreamConfiguration streamConfiguration) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                final var factory = new StreamConfigurationFactory();
                final var streamConfig = factory.create(streamConfiguration);
                jsm.addStream(streamConfig);
                emitter.complete(
                        StreamResult.builder().configuration(streamConfiguration).status(StreamStatus.Created).build());
            } catch (Throwable failure) {
                emitter.fail(new SetupException(String.format("Unable to create stream: %s with message: %s",
                        streamConfiguration.name(), failure.getMessage()), failure));
            }
        });
    }

    private Uni<StreamResult> updateStream(final JetStreamManagement jsm,
            final StreamInfo streamInfo,
            final StreamSetupConfiguration setupConfiguration) {
        return Uni.createFrom().<StreamResult> emitter(emitter -> {
            try {
                final var currentConfiguration = streamInfo.getConfiguration();
                final var factory = new StreamConfigurationFactory();
                final var configuration = factory.create(currentConfiguration, setupConfiguration.configuration());
                if (configuration.isPresent()) {
                    logger.debugf("Updating stream %s", setupConfiguration.configuration().name());
                    jsm.updateStream(configuration.get());
                    emitter.complete(StreamResult.builder().configuration(setupConfiguration.configuration())
                            .status(StreamStatus.Updated).build());
                } else {
                    emitter.complete(StreamResult.builder().configuration(setupConfiguration.configuration())
                            .status(StreamStatus.NotModified).build());
                }
            } catch (Throwable failure) {
                logger.errorf(failure, "message: %s", failure.getMessage());
                emitter.fail(new SetupException(String.format("Unable to update stream: %s with message: %s",
                        setupConfiguration.configuration().name(), failure.getMessage()), failure));
            }
        })
                .onFailure().recoverWithUni(failure -> {
                    if (failure.getCause() instanceof JetStreamApiException && setupConfiguration.overwrite()) {
                        return deleteStream(jsm, setupConfiguration.configuration().name())
                                .onItem().transformToUni(v -> createStream(jsm, setupConfiguration.configuration()));
                    }
                    return Uni.createFrom().failure(failure);
                });
    }

    private Uni<Void> deleteStream(final JetStreamManagement jsm, String streamName) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                jsm.deleteStream(streamName);
                emitter.complete(null);
            } catch (Throwable failure) {
                emitter.fail(new SetupException(String.format("Unable to delete stream: %s with message: %s", streamName,
                        failure.getMessage()), failure));
            }
        });
    }
}
