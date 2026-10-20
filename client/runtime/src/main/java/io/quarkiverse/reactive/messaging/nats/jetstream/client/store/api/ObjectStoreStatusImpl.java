package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.ObjectStoreConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.Stream;
import lombok.Builder;

@Builder
public record ObjectStoreStatusImpl(
        @NonNull Stream stream,
        @NonNull ObjectStoreConfiguration configuration) implements ObjectStoreStatus {

}
