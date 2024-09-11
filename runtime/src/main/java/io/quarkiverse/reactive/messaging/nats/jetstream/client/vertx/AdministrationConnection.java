package io.quarkiverse.reactive.messaging.nats.jetstream.client.vertx;

import java.util.List;

import io.nats.client.api.ConsumerInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.administration.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.administration.SetupResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.administration.StreamState;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueSetupConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.SetupConfiguration;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;

public class AdministrationConnection
        extends io.quarkiverse.reactive.messaging.nats.jetstream.client.administration.AdministrationConnection {
    private final Context context;

    public AdministrationConnection(ConnectionConfiguration connectionConfiguration, ConnectionListener connectionListener,
            Context context) {
        super(connectionConfiguration, connectionListener);
        this.context = context;
    }

    @Override
    public Uni<ConsumerInfo> getConsumerInfo(String stream, String consumerName) {
        return super.getConsumerInfo(stream, consumerName)
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<List<String>> getStreams() {
        return super.getStreams()
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<List<String>> getSubjects(String streamName) {
        return super.getSubjects(streamName)
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<List<String>> getConsumerNames(String streamName) {
        return super.getConsumerNames(streamName)
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<PurgeResult> purgeStream(String streamName) {
        return super.purgeStream(streamName)
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Void> deleteMessage(String stream, long sequence, boolean erase) {
        return super.deleteMessage(stream, sequence, erase)
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<StreamState> getStreamState(String streamName) {
        return super.getStreamState(streamName)
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<List<PurgeResult>> purgeAllStreams() {
        return super.purgeAllStreams()
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<SetupResult> addOrUpdateStream(SetupConfiguration setupConfiguration) {
        return super.addOrUpdateStream(setupConfiguration)
                .emitOn(context::runOnContext);
    }

    @Override
    public Uni<Void> addOrUpdateKeyValueStore(KeyValueSetupConfiguration keyValueSetupConfiguration) {
        return super.addOrUpdateKeyValueStore(keyValueSetupConfiguration)
                .emitOn(context::runOnContext);
    }
}
