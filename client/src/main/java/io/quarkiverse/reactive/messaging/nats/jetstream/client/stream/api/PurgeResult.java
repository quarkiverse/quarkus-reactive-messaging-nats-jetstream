package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import io.nats.client.api.PurgeResponse;
import lombok.NonNull;

/**
 * The response to a request to Purge a stream
 */
public interface PurgeResult {

    static PurgeResult of(String stream, PurgeResponse purgeResponse) {
        return PurgeResultImpl.builder().stream(stream).success(purgeResponse.isSuccess()).purged(purgeResponse.getPurged())
                .build();
    }

    /**
     * Returns the name of the stream.
     *
     * @return the stream name
     */
    @NonNull
    String stream();

    /**
     * Returns true if the server was able to purge the stream
     *
     * @return the result flag
     */
    boolean success();

    /**
     * Returns the number of items purged from the stream
     *
     * @return the count
     */
    long purged();
}
