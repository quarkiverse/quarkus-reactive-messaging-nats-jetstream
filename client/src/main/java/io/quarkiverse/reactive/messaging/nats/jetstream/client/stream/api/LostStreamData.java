package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * Information about lost stream data
 */
public interface LostStreamData {

    /**
     * Get the lost message ids. May be empty
     *
     * @return the list of message ids
     */
    @NonNull
    List<Long> messages();

    /**
     * Get the number of bytes that were lost
     *
     * @return the number of lost bytes
     */
    @NonNull
    Optional<Long> bytes();

}
