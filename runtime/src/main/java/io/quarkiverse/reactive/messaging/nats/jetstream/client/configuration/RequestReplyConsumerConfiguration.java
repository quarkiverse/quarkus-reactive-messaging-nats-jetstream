package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;

public interface RequestReplyConsumerConfiguration {

    String name();

    String stream();

    ConsumerConfiguration consumerConfiguration();

    Duration timeout();

}
