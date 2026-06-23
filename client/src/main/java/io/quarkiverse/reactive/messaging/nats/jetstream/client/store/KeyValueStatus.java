package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import java.util.Objects;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamInfo;
import lombok.Builder;

@Builder
public record KeyValueStatus(StreamInfo streamInfo, KeyValueConfiguration configuration) {

    public KeyValueStatus {
        Objects.requireNonNull(streamInfo, "streamInfo");
        Objects.requireNonNull(configuration, "configuration");
    }
}
