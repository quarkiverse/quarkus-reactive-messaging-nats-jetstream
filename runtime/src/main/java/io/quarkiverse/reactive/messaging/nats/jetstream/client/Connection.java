package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface Connection<T> extends AutoCloseable {

    io.nats.client.Connection getNatsConnection();

    boolean isConnected();

    List<ConnectionListener> listeners();

    Uni<Message<T>> publish(Message<T> message, PublishConfiguration publishConfiguration);

    Uni<Message<T>> publish(Message<T> message, PublishConfiguration publishConfiguration,
            ConsumerConfiguration<T> consumerConfiguration);

    Uni<Consumer> addConsumer(ConsumerConfiguration<T> configuration);

    Uni<Message<T>> next(ConsumerConfiguration<T> configuration, Duration timeout);

    Multi<Message<T>> fetch(FetchConsumerConfiguration<T> configuration);

    Uni<Message<T>> resolve(String streamName, long sequence);

    Uni<Subscription<T>> subscribe(PushConsumerConfiguration<T> configuration);

    Uni<Subscription<T>> subscribe(PullConsumerConfiguration<T> configuration);

    Uni<KeyValueStore<T>> keyValueStore(String bucketName);

    Uni<StreamManagement> streamManagement();

    Uni<KeyValueStoreManagement> keyValueStoreManagement();
}
