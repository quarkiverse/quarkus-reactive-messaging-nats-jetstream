package io.quarkiverse.reactive.messaging.nats.jetstream.client.vertx;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.JetStreamSubscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.MessageSubscribeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.delegates.ConnectionDelegate;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.delegates.MessageDelegate;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageFactory;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;

public class PushSubscribeMessageConnection<K> implements MessageSubscribeConnection {
    private static final Logger logger = Logger.getLogger(PushSubscribeMessageConnection.class);

    private final io.nats.client.Connection connection;
    private final List<ConnectionListener> listeners;
    private final MessageFactory messageFactory;
    private final Context context;
    private final ConnectionDelegate connectionDelegate;
    private final MessageDelegate messageDelegate;

    private final PushConsumerConfiguration<K> consumerConfiguration;
    private final PushSubscribeOptionsFactory optionsFactory;
    private volatile JetStreamSubscription subscription;
    private volatile Dispatcher dispatcher;

    public PushSubscribeMessageConnection(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener,
            Context context,
            PushConsumerConfiguration<K> consumerConfiguration,
            MessageFactory messageFactory,
            PushSubscribeOptionsFactory optionsFactory) throws ConnectionException {
        this.connectionDelegate = new ConnectionDelegate();
        this.messageDelegate = new MessageDelegate();
        this.connection = connectionDelegate.connect(this, connectionConfiguration);
        this.listeners = new ArrayList<>(List.of(connectionListener));
        this.messageFactory = messageFactory;
        this.context = context;
        this.consumerConfiguration = consumerConfiguration;
        this.optionsFactory = optionsFactory;
    }

    @Override
    public <T> Uni<Message<T>> publish(Message<T> message, PublishConfiguration configuration) {
        return messageDelegate.publish(connection, messageFactory, context, message, configuration);
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
        final var subject = consumerConfiguration.subject();
        return Multi.createFrom().<io.nats.client.Message> emitter(emitter -> {
            try {
                final var jetStream = connection.jetStream();
                dispatcher = connection.createDispatcher();
                final var pushOptions = optionsFactory.create(consumerConfiguration);
                subscription = jetStream.subscribe(
                        subject, dispatcher,
                        emitter::emit,
                        false,
                        pushOptions);
            } catch (Throwable e) {
                logger.errorf(
                        e,
                        "Failed subscribing to stream: %s, subject: %s with message: %s",
                        consumerConfiguration.consumerConfiguration().stream(),
                        subject,
                        e.getMessage());
                emitter.fail(e);
            }
        })
                .emitOn(context::runOnContext)
                .map(message -> messageFactory.create(
                        message,
                        traceEnabled,
                        payloadType,
                        context,
                        new ExponentialBackoff(
                                consumerConfiguration.consumerConfiguration().exponentialBackoff(),
                                consumerConfiguration.consumerConfiguration().exponentialBackoffMaxDuration()),
                        consumerConfiguration.consumerConfiguration().ackTimeout()));
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
    public void close() throws Exception {
        try {
            if (subscription.isActive()) {
                subscription.drain(Duration.ofMillis(1000));
            }
        } catch (InterruptedException | IllegalStateException e) {
            logger.warnf("Interrupted while draining subscription");
        }
        try {
            if (subscription != null && dispatcher != null && dispatcher.isActive()) {
                dispatcher.unsubscribe(subscription);
            }
        } catch (Exception e) {
            logger.errorf(e, "Failed to shutdown pull executor");
        }
        connectionDelegate.close(this::connection);
    }

    private Connection connection() {
        return connection;
    }
}
