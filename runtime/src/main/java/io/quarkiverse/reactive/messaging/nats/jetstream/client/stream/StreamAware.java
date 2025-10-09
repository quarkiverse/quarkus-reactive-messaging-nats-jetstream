package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamState;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface StreamAware {

    Multi<String> streamNames();

    Multi<String> subjects(String streamName);

    Uni<PurgeResult> purge(String streamName);

    Uni<Long> firstSequence(String streamName);

    /**
     * Deletes a message, overwriting the message data with garbage
     * This can be considered an expensive (time-consuming) operation, but is more secure.
     *
     * @param stream name of the stream
     * @param sequence the sequence number of the message
     * @param erase whether to erase the message (overwriting with garbage) or only mark it as erased.
     * @throws MessageNotDeletedException when message is not deleted
     */
    Uni<Void> deleteMessage(String stream, long sequence, boolean erase);

    Uni<StreamState> streamState(String streamName);

    Uni<StreamConfiguration> streamConfiguration(String streamName);

    Multi<PurgeResult> purgeAll();

    Uni<Void> addSubject(String streamName, String subject);

    Uni<Void> removeSubject(String streamName, String subject);

    /**
     * Adds streams. The map key is the name of the stream
     */
    Uni<StreamResult> addStreamIfAbsent(StreamConfiguration configuration);

}
