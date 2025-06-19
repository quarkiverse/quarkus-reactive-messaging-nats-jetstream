package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;

public interface FetchConsumerConfiguration<T> extends ConsumerConfiguration<T> {

    Duration timeout();

    Integer batchSize();

    ConsumerConfiguration<T> consumerConfiguration();

}
