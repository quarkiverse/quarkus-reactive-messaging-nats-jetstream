package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.util.Optional;

public interface PullConsumerConfiguration<T> extends ConsumerConfiguration<T> {

    /**
     * The maximum duration a single pull request will wait for messages to be available to pull
     */
    Duration maxExpires();

    /**
     * The size of batch of messages to be pulled in pull mode
     */
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
