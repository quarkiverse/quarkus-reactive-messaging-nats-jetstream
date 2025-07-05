package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface Connection extends AutoCloseable {
    int DEFAULT_MAX_RECONNECT = -1;

    boolean isConnected();

    List<ConnectionListener> listeners();

    Uni<Message<?>> publish(Message<?> message, String stream, String subject);

    Uni<Consumer> addConsumer(String stream, String name, ConsumerConfiguration configuration);

    Uni<Message<?>> next(String stream, String consumer, ConsumerConfiguration configuration, Duration timeout);

    Multi<Message<?>> fetch(String stream, String consumer, FetchConsumerConfiguration configuration);

    Uni<Message<?>> resolve(String stream, long sequence);

    Uni<Subscription> subscribe(String stream, String consumer, PushConsumerConfiguration configuration);

    Uni<Subscription> subscribe(String stream, String consumer, PullConsumerConfiguration configuration);

    Uni<KeyValueStore> keyValueStore(String bucketName);

    Uni<StreamManagement> streamManagement();

    Uni<KeyValueStoreManagement> keyValueStoreManagement();

    void nativeConnection(java.util.function.Consumer<io.nats.client.Connection> connection);
}
