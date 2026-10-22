package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.KeyValueConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.Stream;

public interface KeyValueStatus {

    static KeyValueStatus of(io.nats.client.api.KeyValueStatus keyValueStatus) {
        final var mapper = Mappers.getMapper(KeyValueStatusMapper.class);
        return mapper.map(keyValueStatus);
    }

    /**
     * Gets the info for the stream which backs the bucket. Valid for BackingStore "JetStream"
     *
     * @return the stream info
     */
    @NonNull
    Stream stream();

    /**
     * Gets the configuration object directly
     *
     * @return the configuration.
     */
    @NonNull
    KeyValueConfiguration configuration();

}
