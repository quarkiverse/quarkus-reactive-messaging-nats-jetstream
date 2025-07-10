package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

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
    @WithDefault("1")
    Integer batchSize();

    /**
     * The point in the current batch to tell the server to start the next batch
     */
    Optional<Integer> rePullAt();

    /**
     * The maximum number of waiting pull requests.
     */
    Optional<Integer> maxWaiting();

}
