package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

import io.nats.client.api.ObjectMeta;

/**
 * The ObjectMeta is Object Meta is high level information about an object
 */
public interface ObjectMetadata {

    static ObjectMetadata of(ObjectMeta value) {
        final var mapper = Mappers.getMapper(ObjectMetadataMapper.class);
        return mapper.map(value);
    }

    static ObjectMeta of(ObjectMetadata value) {
        final var mapper = Mappers.getMapper(ObjectMetadataMapper.class);
        return mapper.map(value);
    }

    /**
     * The object name
     *
     * @return the object name
     */
    @NonNull
    String objectName();

    /**
     * The description
     *
     * @return the description text or null
     */
    @NonNull
    Optional<String> description();

    /**
     * Headers may be empty but will not be null. In all cases it will be unmodifiable
     *
     * @return the headers object
     */
    @NonNull
    Map<String, List<String>> headers();

    /**
     * Metadata may be empty but will not be null. In all cases it will be unmodifiable
     *
     * @return the map
     */
    @NonNull
    Map<String, String> metadata();

    /**
     * The ObjectMetaOptions are additional options describing the object
     *
     * @return the object meta data
     */
    @NonNull
    Optional<ObjectMetadataOptions> options();
}
