package io.quarkiverse.reactive.messaging.nats.jetstream.test.client;

import java.time.Duration;
import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PullConfiguration;

public class ClientPullConfiguration implements PullConfiguration {

    @Override
    public Duration maxExpires() {
        return Duration.ofSeconds(3);
    }

    @Override
    public Integer batchSize() {
        return 20;
    }

    @Override
    public Optional<Integer> maxWaiting() {
        return Optional.empty();
    }
}
