package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.KeyValueConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.Stream;
import lombok.Builder;

@Builder
public record KeyValueStatusImpl(Stream stream, KeyValueConfiguration configuration) implements KeyValueStatus {

}
