package io.quarkiverse.reactive.messaging.nats.jetstream.client.vertx;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.nats.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.FetchConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PublishConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.delegates.ConnectionDelegate;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.delegates.MessageDelegate;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageFactory;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;

public class MessageConnection implements io.quarkiverse.reactive.messaging.nats.jetstream.client.MessageConnection {
    private final io.nats.client.Connection connection;
    private final List<ConnectionListener> listeners;
    private final ConnectionDelegate connectionDelegate;
    private final MessageDelegate messageDelegate;
    private final MessageFactory messageFactory;
    private final Context context;

    public MessageConnection(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener,
            MessageFactory messageFactory,
            Context context) {
        this.connectionDelegate = new ConnectionDelegate();
        this.messageDelegate = new MessageDelegate();
        this.connection = connectionDelegate.connect(this, connectionConfiguration);
        this.listeners = new ArrayList<>(List.of(connectionListener));
        this.messageFactory = messageFactory;
        this.context = context;
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
    public boolean isConnected() {
        return connectionDelegate.isConnected(this::connection);
    }

    @Override
    public Uni<Void> flush(Duration duration) {
        return connectionDelegate.flush(this::connection, duration);
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
        connectionDelegate.close(this::connection);
    }

    private Connection connection() {
        return connection;
    }
}
