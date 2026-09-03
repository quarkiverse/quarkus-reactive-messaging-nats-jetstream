package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.ObjectStoreConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.Stream;

public interface ObjectStoreStatus {

    static ObjectStoreStatus of(io.nats.client.api.ObjectStoreStatus objectStoreStatus) {
        final var mapper = Mappers.getMapper(ObjectStoreStatusMapper.class);
        return mapper.map(objectStoreStatus);
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
    ObjectStoreConfiguration configuration();

}
