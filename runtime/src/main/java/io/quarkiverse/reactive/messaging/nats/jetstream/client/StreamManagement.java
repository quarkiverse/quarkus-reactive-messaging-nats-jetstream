package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.ZonedDateTime;
import java.util.List;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.StreamConfiguration;
import io.smallrye.mutiny.Uni;

public interface StreamManagement {

    Uni<Consumer> getConsumer(String stream, String consumerName);

    Uni<List<String>> getStreams();

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

    Uni<List<PurgeResult>> purgeAllStreams();

    Uni<Void> addSubject(String streamName, String subject);

    Uni<Void> removeSubject(String streamName, String subject);

    Uni<List<StreamResult>> addStreams(List<StreamSetupConfiguration> streamConfigurations);
}
