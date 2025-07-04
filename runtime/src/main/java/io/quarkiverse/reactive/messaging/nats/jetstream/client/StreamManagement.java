package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.ZonedDateTime;
import java.util.List;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamState;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.StreamConfiguration;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface StreamManagement {

    Uni<Consumer> getConsumer(String stream, String consumerName);

    Uni<List<String>> getStreamNames();

    Uni<List<String>> getSubjects(String streamName);

    Uni<List<String>> getConsumerNames(String streamName);

    Uni<Void> deleteConsumer(String streamName, String consumerName);

    Uni<Void> pauseConsumer(String streamName, String consumerName, ZonedDateTime pauseUntil);

    Uni<Void> resumeConsumer(String streamName, String consumerName);

    Uni<PurgeResult> purgeStream(String streamName);

    Uni<Long> getFirstSequence(String streamName);

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

    Uni<StreamConfiguration> getStreamConfiguration(String streamName);

    Multi<PurgeResult> purgeAllStreams();

    Uni<Void> addSubject(String streamName, String subject);

    Uni<Void> removeSubject(String streamName, String subject);

    /**
     * Adds streams. The map key is the name of the stream
     */
    Uni<StreamResult> addStream(String name, StreamConfiguration configuration);

}
