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

    Uni<Message<T>> publish(Message<T> message, PublishConfiguration publishConfiguration);

    Uni<Consumer> addConsumer(String consumerName, ConsumerConfiguration<T> configuration);

    Uni<Message<T>> next(String consumerName, ConsumerConfiguration<T> configuration, Duration timeout);

    Multi<Message<T>> fetch(String consumerName, FetchConsumerConfiguration<T> configuration);

    Uni<Message<T>> resolve(String streamName, long sequence);

    Uni<Subscription<T>> subscribe(String consumerName, PushConsumerConfiguration<T> configuration);

    Uni<Subscription<T>> subscribe(String consumerName, PullConsumerConfiguration<T> configuration);

    Uni<KeyValueStore<T>> keyValueStore(String bucketName);

    Uni<StreamManagement> streamManagement();

    Uni<KeyValueStoreManagement> keyValueStoreManagement();
}
