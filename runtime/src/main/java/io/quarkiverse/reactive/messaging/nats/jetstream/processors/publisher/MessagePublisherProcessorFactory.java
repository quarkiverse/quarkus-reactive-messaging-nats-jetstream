package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
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

    public MessagePublisherProcessor create(String channel, String stream, String consumer, Duration retryBackoff) {
        final var streamConfiguration = Optional.ofNullable(configuration.streams().get(stream))
                .orElseThrow(() -> new ConfigurationException("Stream configuration not found for stream: " + stream));
        final var consumerConfiguration = Optional.ofNullable(streamConfiguration.consumers().get(consumer)).orElseThrow(() -> new ConfigurationException(
                        "Consumer configuration not found for stream: " + stream + " and consumer: " + consumer));
        if (consumerConfiguration instanceof PullConsumerConfiguration pullConsumerConfiguration) {
            return new MessagePullPublisherProcessor(channel, stream, consumer, connectionFactory, configuration.connection(),
                    pullConsumerConfiguration, retryBackoff);
        } else if (consumerConfiguration instanceof PushConsumerConfiguration pushConsumerConfiguration) {
            return new MessagePushPublisherProcessor(channel, stream, consumer, connectionFactory, configuration.connection(),
                    pushConsumerConfiguration, retryBackoff);
        } else {
            throw new ConfigurationException("Unsupported consumer configuration type for channel: " + channel
                    + " and consumer: " + consumer + ". Expected PullConsumerConfiguration or PushConsumerConfiguration.");
        }
    }
}
