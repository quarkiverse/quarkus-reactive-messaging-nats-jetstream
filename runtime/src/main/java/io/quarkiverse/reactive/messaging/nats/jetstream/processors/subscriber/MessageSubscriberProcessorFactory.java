package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import io.quarkiverse.reactive.messaging.nats.jetstream.ChannelConfigurationNotExistsException;
import io.quarkiverse.reactive.messaging.nats.jetstream.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PublishConfiguration;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class MessageSubscriberProcessorFactory {
    private final NatsConfiguration configuration;
    private final ConnectionFactory connectionFactory;

    public MessageSubscriberProcessorFactory(NatsConfiguration configuration, ConnectionFactory connectionFactory) {
        this.configuration = configuration;
        this.connectionFactory = connectionFactory;
    }

    public <T> MessageSubscriberProcessor<T> create(String channel) {
        final var publishConfiguration = findPublishConfiguration(channel);
        return new MessageSubscriberProcessor<>(channel, configuration.connection(), connectionFactory, publishConfiguration);
    }

    private PublishConfiguration findPublishConfiguration(String channel) {
        return configuration.jetStream().map(jetStream -> findPublishConfiguration(channel, jetStream))
                .orElseThrow(() -> new ChannelConfigurationNotExistsException(channel));
    }

    private PublishConfiguration findPublishConfiguration(String channel, NatsConfiguration.JetStream configuration) {
        return configuration.streams().entrySet().stream()
                .flatMap(entry -> Optional.ofNullable(entry.getValue().outgoing().get(channel)).stream())
                .findAny().orElseThrow(() -> new ChannelConfigurationNotExistsException(channel));
    }
}
