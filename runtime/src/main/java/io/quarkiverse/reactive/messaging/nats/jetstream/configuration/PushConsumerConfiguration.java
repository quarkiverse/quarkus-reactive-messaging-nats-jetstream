package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

public interface PushConsumerConfiguration {

    ConsumerConfiguration consumerConfiguration();

    PushConfiguration pushConfiguration();

}
