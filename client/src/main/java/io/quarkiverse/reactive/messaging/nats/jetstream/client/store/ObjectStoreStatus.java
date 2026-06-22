package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;


import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamInfo;
import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@Builder
public record ObjectStoreStatus(
        @NonNull StreamInfo streamInfo,
        @NonNull ObjectStoreConfiguration configuration,
        /* If true, indicates the store is sealed and cannot be modified in any way */
        @NonNull Boolean sealed) {
    public ObjectStoreStatus {
        Objects.requireNonNull(streamInfo, "streamInfo");
        Objects.requireNonNull(configuration, "configuration");
        Objects.requireNonNull(sealed, "sealed");
    }

}
