package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.StreamConfiguration;

/**
 * The Stream class contains information about a JetStream stream.
 */
public interface Stream {

    static Stream of(io.nats.client.api.StreamInfo streamInfo) {
        final var mapper = Mappers.getMapper(StreamMapper.class);
        return mapper.map(streamInfo);
    }

    /**
     * Gets the stream configuration. Same as getConfig
     *
     * @return the stream configuration.
     */
    @NonNull
    StreamConfiguration configuration();

    /**
     * Gets the creation time of the stream.
     *
     * @return the creation date and time.
     */
    @NonNull
    ZonedDateTime created();

    /**
     * Gets the stream state.
     *
     * @return the stream state
     */
    @NonNull
    StreamState streamState();

    /**
     * Gets the cluster info
     *
     * @return the cluster info
     */
    @NonNull
    Optional<Cluster> cluster();

    /**
     * Gets the mirror info
     *
     * @return the mirror info
     */
    @NonNull
    Optional<Mirror> mirror();

    /**
     * Gets the source info
     *
     * @return the source info
     */
    @NonNull
    List<Source> sources();

    /**
     * Gets the stream alternates
     *
     * @return the stream alternates
     */
    @NonNull
    List<StreamAlternate> alternates();

    /**
     * Gets the server time the info was gathered
     *
     * @return the server gathered timed
     */
    @NonNull
    Optional<ZonedDateTime> timestamp();

}
