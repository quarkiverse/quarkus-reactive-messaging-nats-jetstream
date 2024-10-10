package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamState;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.FetchConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PublishConfiguration;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface Connection extends AutoCloseable {

    boolean isConnected();

    Uni<Void> flush(Duration duration);

    List<ConnectionListener> listeners();

    void addListener(ConnectionListener listener);

    default void fireEvent(ConnectionEvent event, String message) {
        listeners().forEach(listener -> listener.onEvent(event, message));
    }

    Uni<Consumer> getConsumer(String stream, String consumerName);

    Uni<List<String>> getStreams();

    Uni<List<String>> getSubjects(String streamName);

    Uni<List<String>> getConsumerNames(String streamName);

    Uni<Void> deleteConsumer(String streamName, String consumerName);

    Uni<PurgeResult> purgeStream(String streamName);

    /**
     * Deletes a message, overwriting the message data with garbage
     * This can be considered an expensive (time-consuming) operation, but is more secure.
     *
     * @param stream name of the stream
     * @param sequence the sequence number of the message
     * @param erase whether to erase the message (overwriting with garbage) or only mark it as erased.
     * @throws DeleteException when message is not deleted
     */
    Uni<Void> deleteMessage(String stream, long sequence, boolean erase);

    Uni<StreamState> getStreamState(String streamName);

    Uni<List<PurgeResult>> purgeAllStreams();

    <T> Uni<Message<T>> publish(final Message<T> message, final PublishConfiguration configuration);

    <T> Uni<Message<T>> publish(final Message<T> message, final PublishConfiguration publishConfiguration,
            FetchConsumerConfiguration<T> consumerConfiguration);

    <T> Uni<Message<T>> nextMessage(FetchConsumerConfiguration<T> configuration);

    <T> Multi<Message<T>> nextMessages(FetchConsumerConfiguration<T> configuration);

    <T> Uni<T> getKeyValue(String bucketName, String key, Class<T> valueType);

    <T> Uni<Void> putKeyValue(String bucketName, String key, T value);

    Uni<Void> deleteKeyValue(String bucketName, String key);

    <T> Uni<Message<T>> resolve(String streamName, long sequence);
}
