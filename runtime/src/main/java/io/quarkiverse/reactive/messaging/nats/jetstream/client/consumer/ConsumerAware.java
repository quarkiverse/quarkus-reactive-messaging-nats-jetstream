package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Duration;
import java.time.ZonedDateTime;

public interface ConsumerAware {

    /**
     * Adds a consumer
     *
     * @param configuration The consumer configuration
     * @return The consumer
     */
    <T> Uni<Consumer> addConsumerIfAbsent(ConsumerConfiguration<T> configuration);

    <T> Uni<Consumer> addConsumerIfAbsent(ConsumerConfiguration<T> configuration, PushConfiguration pushConfiguration);

    <T> Uni<Consumer> addConsumerIfAbsent(ConsumerConfiguration<T> configuration, PullConfiguration pullConfiguration);

    Uni<Consumer> consumer(String stream, String consumerName);

    Multi<String> consumerNames(String streamName);

    Uni<Void> deleteConsumer(String streamName, String consumerName);

    Uni<Void> pauseConsumer(String streamName, String consumerName, ZonedDateTime pauseUntil);

    Uni<Void> resumeConsumer(String streamName, String consumerName);

    <T> Uni<Message<T>> next(ConsumerConfiguration<T> configuration, Duration timeout);

    <T> Multi<Message<T>> fetch(ConsumerConfiguration<T> configuration, FetchConfiguration fetchConfiguration);

    <T> Uni<Message<T>> resolve(String stream, long sequence);

    <T> Multi<Message<T>> subscribe(ConsumerConfiguration<T> configuration, PullConfiguration pullConfiguration);

    <T> Multi<Message<T>> subscribe(ConsumerConfiguration<T> configuration, PushConfiguration pushConfiguration);

    <T> Multi<Message<T>> subscribe(ConsumerConfiguration<T> configuration, PullConfiguration pullConfiguration, ConsumerListener<T> listener);

    <T> Multi<Message<T>> subscribe(ConsumerConfiguration<T> configuration, PushConfiguration pushConfiguration, ConsumerListener<T> listener);

}
