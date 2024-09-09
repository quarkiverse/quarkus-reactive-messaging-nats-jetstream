package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;

public interface MessagePublisherConfiguration {

    String channel();

    String subject();

    Duration retryBackoff();
}
