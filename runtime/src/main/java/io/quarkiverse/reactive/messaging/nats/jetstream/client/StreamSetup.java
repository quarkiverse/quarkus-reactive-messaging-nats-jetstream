package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.List;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamSetupConfiguration;
import io.smallrye.mutiny.Uni;

public interface StreamSetup {

    Uni<List<StreamResult>> addStreams(List<StreamSetupConfiguration> streamConfigurations);

}
