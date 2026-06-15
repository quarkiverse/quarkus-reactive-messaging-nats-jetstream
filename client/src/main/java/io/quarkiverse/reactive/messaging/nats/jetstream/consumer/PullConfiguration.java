package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import java.time.Duration;
import java.util.Optional;

public interface PullConfiguration {

    /**
     * The max amount of expiry time for the server to allow on pull requests.
     */
    Optional<Duration> maxExpires();

    /**
     * The maximum number of waiting pull requests.
     */
    Optional<Integer> maxWaiting();
}
