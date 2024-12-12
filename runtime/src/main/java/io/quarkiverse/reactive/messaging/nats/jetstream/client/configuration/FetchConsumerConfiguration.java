package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;

public interface FetchConsumerConfiguration<T> {

    Duration timeout();

    Integer batchSize();

    ConsumerConfiguration<T> consumerConfiguration();

    static <P> FetchConsumerConfiguration<P> of(final ConsumerConfiguration<P> configuration,
            final Duration fetchTimeout,
            final Integer batchSize) {
        return new FetchConsumerConfiguration<P>() {
            @Override
            public Duration timeout() {
                return fetchTimeout;
            }

            @Override
            public Integer batchSize() {
                return batchSize;
            }

            @Override
            public ConsumerConfiguration<P> consumerConfiguration() {
                return configuration;
            }
        };

    }
}
