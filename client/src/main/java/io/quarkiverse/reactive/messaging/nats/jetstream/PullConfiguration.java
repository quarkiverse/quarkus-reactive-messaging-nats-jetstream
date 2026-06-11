package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.time.Duration;
import java.util.Optional;

/**
 * Represents the configuration settings for pull-based message fetching from a streaming or messaging system.
 * This interface defines methods to configure and control the behavior of pull-mode message retrieval.
 */
public interface PullConfiguration {

    /**
     * The maximum duration of a single pull request will wait for messages to be available to pull
     */
    Duration maxExpires();

    /**
     * The size of batch of messages to be pulled in pull mode
     */
    Integer batchSize();

    /**
     * The maximum number of waiting pull requests.
     */
    Optional<Integer> maxWaiting();
}
