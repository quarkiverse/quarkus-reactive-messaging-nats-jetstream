package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface Connection<T> extends AutoCloseable {
    int DEFAULT_MAX_RECONNECT = -1;

    boolean isConnected();

    List<ConnectionListener> listeners();

    Uni<Message<T>> publish(Message<T> message, String stream, String subject);

    Uni<Consumer> addConsumer(String stream, ConsumerConfiguration<T> configuration);

    Uni<Message<T>> next(String stream, ConsumerConfiguration<T> configuration, Duration timeout);

    Multi<Message<T>> fetch(String stream, FetchConsumerConfiguration<T> configuration);

    Uni<Message<T>> resolve(String stream, long sequence);

    Uni<Subscription<T>> subscribe(String stream, PushConsumerConfiguration<T> configuration);

    Uni<Subscription<T>> subscribe(String stream, PullConsumerConfiguration<T> configuration);

    Uni<KeyValueStore<T>> keyValueStore(String bucketName);

    Uni<StreamManagement> streamManagement();

    Uni<KeyValueStoreManagement> keyValueStoreManagement();

    void nativeConnection(java.util.function.Consumer<io.nats.client.Connection> connection);
}
