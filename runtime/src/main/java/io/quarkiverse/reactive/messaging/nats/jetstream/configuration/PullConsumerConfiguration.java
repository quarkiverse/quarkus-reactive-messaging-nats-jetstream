package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

public interface PullConsumerConfiguration {

    ConsumerConfiguration consumerConfiguration();

    PullConfiguration pullConfiguration();
}
