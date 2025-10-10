package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

import java.time.Duration;
import java.util.Optional;

import io.smallrye.config.WithDefault;

public interface PullConfiguration {

    /**
     * The maximum duration of a single pull request will wait for messages to be available to pull
     */
    @WithDefault("1s")
    Duration maxExpires();

    /**
     * The size of batch of messages to be pulled in pull mode
     */
    @WithDefault("100")
    Integer batchSize();

    /**
     * The point in the current batch to tell the server to start the next batch
     */
    @WithDefault("50")
    Integer rePullAt();

    /**
     * The maximum number of waiting pull requests.
     */
    Optional<Integer> maxWaiting();
}
