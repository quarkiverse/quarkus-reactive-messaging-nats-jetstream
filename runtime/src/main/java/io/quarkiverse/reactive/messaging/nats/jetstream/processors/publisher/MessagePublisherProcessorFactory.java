package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PullConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PushConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConfigurationException;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper.PullConsumerConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper.PushConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper.PushConsumerConfigurationMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class MessagePublisherProcessorFactory {
    private final ConnectorConfiguration configuration;
    private final Client client;
    private final PullConsumerConfigurationMapper pullConsumerConfigurationMapper;
    private final PushConsumerConfigurationMapper pushConsumerConfigurationMapper;

    public MessagePublisherProcessor<?> create(String channel, String stream, String consumer, Duration retryBackoff) {
        return createPullPublisherProcessor(channel, stream, consumer, retryBackoff)
                .orElseGet(() -> createPushPublisherProcessor(channel, stream, consumer, retryBackoff)
                        .orElseThrow(() -> new ConfigurationException(
                                "Consumer configuration not found for stream: " + stream + " and consumer: " + consumer)));
    }

    private Optional<MessagePublisherProcessor<?>> createPullPublisherProcessor(String channel, String stream, String consumer,
            Duration retryBackoff) {
        return findPullConsumerConfiguration(stream, consumer)
                .map(tuple -> createPullPublisherProcessor(channel, stream, consumer, retryBackoff,
                        tuple.consumerConfiguration(), tuple.pullConfiguration()));
    }

    private <T> MessagePublisherProcessor<?> createPullPublisherProcessor(String channel, String stream, String consumer,
            Duration retryBackoff, ConsumerConfiguration<T> consumerConfiguration, PullConfiguration pullConfiguration) {
        return new MessagePullPublisherProcessor<>(channel, stream, consumer, client, consumerConfiguration, pullConfiguration,
                retryBackoff);
    }

    private Optional<MessagePublisherProcessor<?>> createPushPublisherProcessor(String channel, String stream, String consumer,
            Duration retryBackoff) {
        return findPushConsumerConfiguration(stream, consumer)
                .map(tuple -> createPushPublisherProcessor(channel, stream, consumer, retryBackoff,
                        tuple.consumerConfiguration(), tuple.pushConfiguration()));
    }

    private <T> MessagePublisherProcessor<?> createPushPublisherProcessor(String channel, String stream, String consumer,
            Duration retryBackoff,
            ConsumerConfiguration<T> consumerConfiguration, PushConfiguration pushConfiguration) {
        return new MessagePushPublisherProcessor<>(channel, stream, consumer, client,
                consumerConfiguration, pushConfiguration, retryBackoff);
    }

    private <T> Optional<PushConsumerConfiguration<T>> findPushConsumerConfiguration(String stream,
            String consumer) {
        return pushConsumerConfigurationMapper.<T> map(stream, configuration).stream()
                .filter(tuple -> tuple.consumerConfiguration().name().equals(consumer)).findAny();
    }

    private <T> Optional<PullConsumerConfiguration<T>> findPullConsumerConfiguration(String stream, String consumer) {
        return pullConsumerConfigurationMapper.<T> map(stream, configuration).stream()
                .filter(tuple -> tuple.consumerConfiguration().name().equals(consumer)).findAny();
    }
}
