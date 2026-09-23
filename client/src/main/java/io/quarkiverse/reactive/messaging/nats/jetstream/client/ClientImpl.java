package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValue;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueManagement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectStore;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectStoreManagement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamManagement;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.extern.jbosslog.JBossLog;

@SuppressWarnings("DuplicatedCode")
@JBossLog
class ClientImpl implements Client {
    private final NativeConnection connection;
    private final Context context;
    private final Publisher publisher;
    private final Consumer consumer;
    private final StreamManagement streamManagement;
    private final KeyValueManagement keyValueManagement;
    private final ObjectStoreManagement objectStoreManagement;

    ClientImpl(@NonNull NativeConnection connection,
            @NonNull Context context,
            @NonNull TracerFactory tracerFactory,
            @NonNull Serializer serializer) {
        this.connection = connection;
        this.context = context;
        this.publisher = new PublisherImpl(connection, context, serializer, tracerFactory);
        this.consumer = new ConsumerImpl(connection, context, serializer, tracerFactory);
        this.streamManagement = new StreamManagementImpl(connection, context);
        this.keyValueManagement = new KeyValueManagementImpl(connection, context);
        this.objectStoreManagement = new ObjectStoreManagementImpl(connection, context);
    }

    @Override
    public <T> @NonNull Uni<Message<T>> publish(
            @NonNull Message<T> message, @NonNull final String stream,
            @NonNull final String subject) {
        return publisher.publish(message, stream, subject);
    }

    @Override
    public <T> @NonNull Uni<org.eclipse.microprofile.reactive.messaging.Message<T>> next(@NonNull final String stream,
            @NonNull final String consumer,
            @NonNull final Duration timeout) {
        return this.consumer.next(stream, consumer, timeout);
    }

    @Override
    public @NonNull <T> Uni<org.eclipse.microprofile.reactive.messaging.Message<T>> next(@NonNull String stream,
            @NonNull String consumer, @NonNull Duration timeout, @NonNull Class<T> clazz) {
        return this.consumer.next(stream, consumer, timeout, clazz);
    }

    @Override
    public @NonNull <T> Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> fetch(@NonNull final String stream,
            @NonNull final String consumer,
            @NonNull final Duration timeout, final int batchSize) {
        return this.consumer.fetch(stream, consumer, timeout, batchSize);
    }

    @Override
    public @NonNull <T> Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> fetch(@NonNull String stream,
            @NonNull String consumer, @NonNull Duration timeout, int batchSize, @NonNull Class<T> clazz) {
        return this.consumer.fetch(stream, consumer, timeout, batchSize, clazz);
    }

    @Override
    public @NonNull <T> Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> subscribe(final @NonNull String stream,
            final @NonNull String consumer,
            final @NonNull Duration timeout, final int batchSize) {
        return this.consumer.subscribe(stream, consumer, timeout, batchSize);
    }

    @Override
    public @NonNull <T> Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> subscribe(@NonNull String stream,
            @NonNull String consumer, @NonNull Duration timeout, int batchSize,
            @NonNull Class<T> clazz) {
        return this.consumer.subscribe(stream, consumer, timeout, batchSize, clazz);
    }

    @Override
    public @NonNull <T> Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> subscribe(@NonNull String stream,
            @NonNull String consumer) {
        return this.consumer.subscribe(stream, consumer);
    }

    @Override
    public @NonNull <T> Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> subscribe(@NonNull String stream,
            @NonNull String consumer, @NonNull Class<T> clazz) {
        return this.consumer.subscribe(stream, consumer, clazz);
    }

    @Override
    public @NonNull StreamManagement streamManagement() {
        return streamManagement;
    }

    @Override
    public @NonNull ConsumerManagement consumerManagement(@NonNull String stream) {
        return new ConsumerManagementImpl(stream, connection, context);
    }

    @Override
    public @NonNull KeyValueManagement keyValueManagement() {
        return keyValueManagement;
    }

    @Override
    public @NonNull ObjectStoreManagement objectStoreManagement() {
        return objectStoreManagement;
    }

    @Override
    public @NonNull ObjectStore objectStore(@NonNull final String bucketName) {
        return new ObjectStoreImpl(bucketName, connection, context);
    }

    @Override
    public @NonNull KeyValue keyValue(@NonNull final String bucketName) {
        return new KeyValueImpl(bucketName, connection, context);
    }

    @Override
    public boolean closed() {
        return connection.getStatus() == io.nats.client.Connection.Status.CLOSED;
    }

    @Override
    public @NonNull NativeConnection nativeConnection() {
        return connection;
    }

    @Override
    public void close() throws Exception {
        if (connection.getStatus() != io.nats.client.Connection.Status.CLOSED) {
            connection.close();
        }
    }
}
