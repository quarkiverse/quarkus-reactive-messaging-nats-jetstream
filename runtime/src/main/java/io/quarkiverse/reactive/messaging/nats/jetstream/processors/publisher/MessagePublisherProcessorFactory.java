package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConfigurationException;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.JetStreamConfiguration;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

@ApplicationScoped
public class MessagePublisherProcessorFactory {
    private final JetStreamConfiguration configuration;
    private final ConnectionFactory connectionFactory;

    public MessagePublisherProcessorFactory(JetStreamConfiguration configuration, ConnectionFactory connectionFactory) {
        this.configuration = configuration;
        this.connectionFactory = connectionFactory;
    }

    public MessagePublisherProcessor<?> create(String channel, String stream, String consumer, Duration retryBackoff) {
        final var streamConfiguration = Optional.ofNullable(configuration.streams().get(stream)).orElseThrow(() -> new ConfigurationException("Stream configuration not found for stream: " + stream));
        final var consumerConfiguration = Stream.of(streamConfiguration.consumers())
                .filter(configuration -> consumer.equals(configuration.name()))
                .findAny().orElseThrow(() -> new ConfigurationException("Consumer configuration not found for stream: " + stream + " and consumer: " + consumer));
        if (consumerConfiguration instanceof PullConsumerConfiguration) {
            return new MessagePullPublisherProcessor<>(channel, stream, connectionFactory, configuration.connection(),
                    (PullConsumerConfiguration<?>) consumerConfiguration, retryBackoff);
        } else {
            return new MessagePushPublisherProcessor<>(channel, stream, connectionFactory, configuration.connection(),
                    (PushConsumerConfiguration<?>) consumerConfiguration, retryBackoff);
        }
    }
}
