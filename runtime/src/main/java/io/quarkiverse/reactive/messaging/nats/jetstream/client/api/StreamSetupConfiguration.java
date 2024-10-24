package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.util.List;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamBuildConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.StreamConfiguration;
import lombok.Builder;

@Builder
public record StreamSetupConfiguration(StreamConfiguration configuration, boolean overwrite) {

    public static List<StreamSetupConfiguration> of(JetStreamBuildConfiguration configuration) {
        return configuration.streams()
                .stream().map(c -> StreamSetupConfiguration.builder()
                        .configuration(StreamConfiguration.of(c))
                        .overwrite(c.overwrite())
                        .build())
                .toList();
    }
}
