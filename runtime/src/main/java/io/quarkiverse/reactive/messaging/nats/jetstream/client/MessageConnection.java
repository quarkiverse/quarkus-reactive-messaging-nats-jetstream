package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.FetchConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PublishConfiguration;
import io.smallrye.mutiny.Uni;

public interface MessageConnection extends Connection {

    <T> Uni<Message<T>> publish(final Message<T> message, final PublishConfiguration configuration);

    <T> Uni<Message<T>> publish(final Message<T> message, final PublishConfiguration publishConfiguration,
            FetchConsumerConfiguration<T> consumerConfiguration);

    <T> Uni<Message<T>> nextMessage(FetchConsumerConfiguration<T> configuration);

    <T> Uni<T> getKeyValue(String bucketName, String key, Class<T> valueType);

    <T> Uni<Void> putKeyValue(String bucketName, String key, T value);

    Uni<Void> deleteKeyValue(String bucketName, String key);

    <T> Uni<Message<T>> resolve(String streamName, long sequence);
}
