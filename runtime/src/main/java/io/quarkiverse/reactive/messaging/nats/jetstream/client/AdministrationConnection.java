package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.administration.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueSetupConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.SetupConfiguration;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface AdministrationConnection extends Connection {

    Uni<Consumer> getConsumer(String stream, String consumerName);

    Uni<List<String>> getStreams();

    Uni<List<String>> getSubjects(String streamName);

    Uni<List<String>> getConsumerNames(String streamName);

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

    Uni<Stream> addOrUpdateStream(SetupConfiguration setupConfiguration);

    Uni<Void> addOrUpdateKeyValueStore(KeyValueSetupConfiguration keyValueSetupConfiguration);
}
