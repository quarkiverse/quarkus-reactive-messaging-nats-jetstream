package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConfigurationException;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.JetStreamConfiguration;

@ApplicationScoped
public class MessagePublisherProcessorFactory {
    private final JetStreamConfiguration configuration;
    private final ConnectionFactory connectionFactory;

    public MessagePublisherProcessorFactory(JetStreamConfiguration configuration, ConnectionFactory connectionFactory) {
        this.configuration = configuration;
        this.connectionFactory = connectionFactory;
    }

    @SuppressWarnings("unchecked")
    public <T> MessagePublisherProcessor<T> create(String channel, String stream, String consumer, Duration retryBackoff) {
        final var streamConfiguration = Optional.ofNullable(configuration.streams().get(stream))
                .orElseThrow(() -> new ConfigurationException("Stream configuration not found for stream: " + stream));
        return (MessagePublisherProcessor<T>) findPullConsumerConfiguration(streamConfiguration, consumer)
                .map(pullConsumerConfiguration -> create(channel, stream, consumer, retryBackoff, pullConsumerConfiguration))
                .orElseGet(() -> findPushConsumerConfiguration(streamConfiguration, consumer)
                        .map(pushConsumerConfiguration -> create(channel, stream, consumer, retryBackoff,
                                pushConsumerConfiguration))
                        .orElseThrow(() -> new ConfigurationException(
                                "Consumer configuration not found for stream: " + stream + " and consumer: " + consumer)));

    }

    private <T> MessagePublisherProcessor<T> create(String channel, String stream, String consumer, Duration retryBackoff,
            PullConsumerConfiguration pullConsumerConfiguration) {
        return new MessagePullPublisherProcessor<>(channel, stream, consumer, connectionFactory, configuration.connection(),
                pullConsumerConfiguration, retryBackoff);
    }

    private <T> MessagePublisherProcessor<T> create(String channel, String stream, String consumer, Duration retryBackoff,
            PushConsumerConfiguration pushConsumerConfiguration) {
        return new MessagePushPublisherProcessor<>(channel, stream, consumer, connectionFactory, configuration.connection(),
                pushConsumerConfiguration, retryBackoff);
    }

    private Optional<PushConsumerConfiguration> findPushConsumerConfiguration(StreamConfiguration streamConfiguration,
            String consumer) {
        return Optional.ofNullable(streamConfiguration.pushConsumers().get(consumer));
    }

    private Optional<PullConsumerConfiguration> findPullConsumerConfiguration(StreamConfiguration streamConfiguration,
            String consumer) {
        return Optional.ofNullable(streamConfiguration.pullConsumers().get(consumer));
    }
}
