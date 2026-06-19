package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import lombok.Builder;
import lombok.NonNull;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Builder
public record PullConfiguration(
        /*
         * The max amount of expiry time for the server to allow on pull requests.
         */
        @NonNull Optional<Long> maxWaiting,

        /*
         * The maximum duration a single pull request will wait for messages to be available to pull..
         */
        @NonNull Optional<Duration> maxRequestExpires,

        /*
         * The maximum batch size a single pull request can make. When set with MaxRequestMaxBytes,
         * the batch size will be constrained by whichever limit is hit first.
         */
        @NonNull Optional<Long> maxRequestBatch,

        /*
         * The maximum total bytes that can be requested in a given batch. When set with MaxRequestBatch,
         * the batch size will be constrained by whichever limit is hit first.
         */
        @NonNull Optional<Long> maxRequestMaxBytes) {

    public PullConfiguration {
        Objects.requireNonNull(maxWaiting, "maxWaiting");
        Objects.requireNonNull(maxRequestExpires, "maxRequestExpires");
        Objects.requireNonNull(maxRequestBatch, "maxRequestBatch");
        Objects.requireNonNull(maxRequestMaxBytes, "maxRequestMaxBytes");
    }

}
