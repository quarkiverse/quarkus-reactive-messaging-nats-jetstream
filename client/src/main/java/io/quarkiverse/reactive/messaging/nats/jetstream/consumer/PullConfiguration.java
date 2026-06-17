package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import lombok.Builder;

import java.time.Duration;
import java.util.Optional;

@Builder
public record PullConfiguration(Optional<Long> maxWaiting,
                                Optional<Duration> maxRequestExpires,
                                Optional<Long> maxRequestBatch,
                                Optional<Long> maxRequestMaxBytes) {

    /**
     * The max amount of expiry time for the server to allow on pull requests.
     */
    @Override
    public Optional<Long> maxWaiting() {
        return maxWaiting;
    }

    /**
     * The maximum duration a single pull request will wait for messages to be available to pull..
     */
    @Override
    public Optional<Duration> maxRequestExpires() {
        return maxRequestExpires;
    }

    /**
     * The maximum batch size a single pull request can make. When set with MaxRequestMaxBytes,
     * the batch size will be constrained by whichever limit is hit first.
     */
    @Override
    public Optional<Long> maxRequestBatch() {
        return maxRequestBatch;
    }

    /**
     * The maximum total bytes that can be requested in a given batch. When set with MaxRequestBatch,
     * the batch size will be constrained by whichever limit is hit first.
     */
    @Override
    public Optional<Long> maxRequestMaxBytes() {
        return maxRequestMaxBytes;
    }

}
