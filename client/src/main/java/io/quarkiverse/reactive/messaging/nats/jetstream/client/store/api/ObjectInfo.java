package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

/**
 * The ObjectInfo is Object Meta Information plus instance information
 */
public interface ObjectInfo {

    static ObjectInfo of(io.nats.client.api.ObjectInfo objectInfo) {
        final var mapper = Mappers.getMapper(ObjectInfoMapper.class);
        return mapper.map(objectInfo);
    }

    static io.nats.client.api.ObjectInfo of(ObjectInfo objectInfo) {
        final var mapper = Mappers.getMapper(ObjectInfoMapper.class);
        return mapper.map(objectInfo);
    }

    /**
     * the bucket name
     *
     * @return the name
     */
    @NonNull
    String bucket();

    /**
     * the bucket nuid
     *
     * @return the nuid
     */
    @NonNull
    Optional<String> nuId();

    /**
     * The size of the object
     *
     * @return the size in bytes
     */
    long size();

    /**
     * When the object was last modified
     *
     * @return the last modified date
     */
    @NonNull
    Optional<ZonedDateTime> modified();

    /**
     * The total number of chunks in the object
     *
     * @return the number of chunks
     */
    long chunks();

    /**
     * The digest string for the object
     *
     * @return the digest
     */
    @NonNull
    Optional<String> digest();

    /**
     * Whether the object is deleted
     *
     * @return the deleted state
     */
    boolean deleted();

    /**
     * The full object metadata object
     *
     * @return the ObjectMeta
     */
    @NonNull
    ObjectMetadata metadata();

}
