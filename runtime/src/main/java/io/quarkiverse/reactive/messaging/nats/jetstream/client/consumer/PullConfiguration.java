package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import java.time.Duration;
import java.util.Optional;

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
