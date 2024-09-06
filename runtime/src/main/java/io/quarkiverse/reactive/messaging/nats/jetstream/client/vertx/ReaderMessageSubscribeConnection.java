package io.quarkiverse.reactive.messaging.nats.jetstream.client.vertx;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.JetStreamStatusException;
import io.nats.client.JetStreamSubscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.MessageSubscribeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.delegates.ConnectionDelegate;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.delegates.MessageDelegate;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;

public class ReaderMessageSubscribeConnection<K> implements MessageSubscribeConnection {
    private final static Logger logger = Logger.getLogger(ReaderMessageSubscribeConnection.class);

    private final io.nats.client.Connection connection;
    private final List<ConnectionListener> listeners;
    private final MessageFactory messageFactory;
    private final Context context;
    private final ConnectionDelegate connectionDelegate;
    private final MessageDelegate messageDelegate;

    private final ReaderConsumerConfiguration<K> consumerConfiguration;
    private final io.nats.client.JetStreamReader reader;
    private final JetStreamSubscription subscription;
    private final JetStreamInstrumenter instrumenter;

    public ReaderMessageSubscribeConnection(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener,
            Context context,
            JetStreamInstrumenter instrumenter,
            ReaderConsumerConfiguration<K> consumerConfiguration,
            MessageFactory messageFactory) throws ConnectionException {
        this.connectionDelegate = new ConnectionDelegate();
        this.messageDelegate = new MessageDelegate();
        this.connection = connectionDelegate.connect(this, connectionConfiguration);
        this.listeners = new ArrayList<>(List.of(connectionListener));
        this.messageFactory = messageFactory;
        this.context = context;
        this.consumerConfiguration = consumerConfiguration;
        this.instrumenter = instrumenter;
        try {
            final var jetStream = connection.jetStream();
            final var optionsFactory = new PullSubscribeOptionsFactory();
            this.subscription = jetStream.subscribe(consumerConfiguration.subject(),
                    optionsFactory.create(consumerConfiguration));
            this.reader = subscription.reader(consumerConfiguration.maxRequestBatch(), consumerConfiguration.rePullAt());
        } catch (Throwable failure) {
            close();
            throw new ReaderException(failure);
        }
    }

    @Override
    public <T> Uni<Message<T>> publish(Message<T> message, PublishConfiguration configuration) {
        return messageDelegate.publish(connection, messageFactory, context, instrumenter, message, configuration);
    }

    @Override
    public <T> Uni<Message<T>> nextMessage(FetchConsumerConfiguration<T> configuration) {
        return messageDelegate.nextMessage(connection, context, messageFactory, configuration);
    }

    @Override
    public <T> Uni<Void> addOrUpdateConsumer(FetchConsumerConfiguration<T> configuration) {
        return messageDelegate.addOrUpdateConsumer(connection, context, configuration);
    }

    @Override
    public <T> Uni<T> getKeyValue(String bucketName, String key, Class<T> valueType) {
        return messageDelegate.getKeyValue(connection, context, messageFactory, bucketName, key, valueType);
    }

    @Override
    public <T> Uni<Void> putKeyValue(String bucketName, String key, T value) {
        return messageDelegate.putKeyValue(connection, context, messageFactory, bucketName, key, value);
    }

    @Override
    public Uni<Void> deleteKeyValue(String bucketName, String key) {
        return messageDelegate.deleteKeyValue(connection, context, bucketName, key);
    }

    @Override
    public <T> Uni<Message<T>> resolve(String streamName, long sequence) {
        return messageDelegate.resolve(connection, context, messageFactory, streamName, sequence);
    }

    @Override
    public Multi<Message<?>> subscribe() {
        boolean traceEnabled = consumerConfiguration.consumerConfiguration().traceEnabled();
        Class<?> payloadType = consumerConfiguration.consumerConfiguration().payloadType().orElse(null);
        ExecutorService pullExecutor = Executors.newSingleThreadExecutor(JetstreamWorkerThread::new);
        return Multi.createBy().repeating()
                .supplier(this::nextMessage)
                .until(message -> !subscription.isActive())
                .runSubscriptionOn(pullExecutor)
                .emitOn(context::runOnContext)
                .flatMap(message -> createMulti(message.orElse(null), traceEnabled, payloadType, context));
    }

    @Override
    public boolean isConnected() {
        return connectionDelegate.isConnected(this::connection);
    }

    @Override
    public Uni<Void> flush(Duration duration) {
        return connectionDelegate.flush(this::connection, duration)
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
    public void close() {
        try {
            reader.stop();
        } catch (Throwable e) {
            logger.warnf("Failed to stop reader with message %s", e.getMessage());
        }
        try {
            if (subscription.isActive()) {
                subscription.drain(Duration.ofMillis(1000));
            }
        } catch (Throwable e) {
            logger.warnf("Interrupted while draining subscription");
        }
        try {
            if (subscription.isActive()) {
                subscription.unsubscribe();
            }
        } catch (Throwable e) {
            logger.warnf("Failed to unsubscribe subscription with message %s", e.getMessage());
        }
        connectionDelegate.close(this::connection);
    }

    private Optional<io.nats.client.Message> nextMessage() {
        try {
            return Optional.ofNullable(reader.nextMessage(consumerConfiguration.maxRequestExpires().orElse(Duration.ZERO)));
        } catch (JetStreamStatusException e) {
            logger.debugf(e, e.getMessage());
            return Optional.empty();
        } catch (IllegalStateException e) {
            logger.debugf(e, "The subscription became inactive for stream: %s",
                    consumerConfiguration.consumerConfiguration().stream());
            return Optional.empty();
        } catch (InterruptedException e) {
            logger.debugf(e, "The reader was interrupted for stream: %s",
                    consumerConfiguration.consumerConfiguration().stream());
            return Optional.empty();
        } catch (Throwable throwable) {
            logger.warnf(throwable, "Error reading next message from stream: %s",
                    consumerConfiguration.consumerConfiguration().stream());
            return Optional.empty();
        }
    }

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<K>> createMulti(io.nats.client.Message message,
            boolean tracingEnabled, Class<?> payloadType, Context context) {
        if (message == null || message.getData() == null) {
            return Multi.createFrom().empty();
        } else {
            return Multi.createFrom()
                    .item(() -> messageFactory.create(message, tracingEnabled, payloadType, context, new ExponentialBackoff(
                            consumerConfiguration.consumerConfiguration().exponentialBackoff(),
                            consumerConfiguration.consumerConfiguration().exponentialBackoffMaxDuration()),
                            consumerConfiguration.consumerConfiguration().ackTimeout()));
        }
    }

    private io.nats.client.Connection connection() {
        return connection;
    }
}
