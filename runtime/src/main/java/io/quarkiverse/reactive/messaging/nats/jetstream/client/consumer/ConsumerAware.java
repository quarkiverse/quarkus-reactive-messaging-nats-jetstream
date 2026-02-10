package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface ConsumerAware {

    /**
     * Adds a consumer
     *
     * @param configuration The consumer configuration
     * @return The consumer
     */
    Uni<Consumer> addConsumerIfAbsent(ConsumerConfiguration configuration);

    Uni<Consumer> addConsumerIfAbsent(ConsumerConfiguration configuration, PushConfiguration pushConfiguration);

    Uni<Consumer> addConsumerIfAbsent(ConsumerConfiguration configuration, PullConfiguration pullConfiguration);

    Uni<Consumer> consumer(String stream, String consumerName);

    Multi<String> consumerNames(String streamName);

    Uni<Void> deleteConsumer(String streamName, String consumerName);

    Uni<Void> pauseConsumer(String streamName, String consumerName, ZonedDateTime pauseUntil);

    Uni<Void> resumeConsumer(String streamName, String consumerName);

    <T> Uni<Message<T>> next(ConsumerConfiguration configuration, Duration timeout);

    <T> Uni<Message<T>> next(ConsumerConfiguration configuration, Duration timeout, Class<T> payloadType);

    <T> Multi<Message<T>> fetch(ConsumerConfiguration configuration, FetchConfiguration fetchConfiguration);

    <T> Multi<Message<T>> fetch(ConsumerConfiguration configuration, FetchConfiguration fetchConfiguration,
            Class<T> payloadType);

    <T> Uni<Message<T>> resolve(String stream, long sequence);

    <T> Multi<Message<T>> subscribe(ConsumerConfiguration configuration, PullConfiguration pullConfiguration);

    <T> Multi<Message<T>> subscribe(ConsumerConfiguration configuration, PullConfiguration pullConfiguration,
            Class<T> payloadType);

    <T> Multi<Message<T>> subscribe(ConsumerConfiguration configuration, PullConfiguration pullConfiguration,
            ConsumerListener<T> listener);

    <T> Multi<Message<T>> subscribe(ConsumerConfiguration configuration, PullConfiguration pullConfiguration,
            ConsumerListener<T> listener, Class<T> payloadType);

    <T> Multi<Message<T>> subscribe(ConsumerConfiguration configuration, PushConfiguration pushConfiguration);

    <T> Multi<Message<T>> subscribe(ConsumerConfiguration configuration, PushConfiguration pushConfiguration,
            Class<T> payloadType);

    <T> Multi<Message<T>> subscribe(ConsumerConfiguration configuration, PushConfiguration pushConfiguration,
            ConsumerListener<T> listener);

    <T> Multi<Message<T>> subscribe(ConsumerConfiguration configuration, PushConfiguration pushConfiguration,
            ConsumerListener<T> listener, Class<T> payloadType);

}
