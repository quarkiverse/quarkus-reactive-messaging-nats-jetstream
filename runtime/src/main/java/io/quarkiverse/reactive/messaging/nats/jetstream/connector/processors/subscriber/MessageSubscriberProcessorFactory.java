package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.subscriber;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientRegistry;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.PublisherChannelConfigurationFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class MessageSubscriberProcessorFactory {
    private final ClientRegistry clientRegistry;
    private final PublisherChannelConfigurationFactory publisherChannelConfigurationFactory;

    public <T> MessageSubscriberProcessor<T> create(@NonNull final Config configuration) {
        final var channelConfiguration = publisherChannelConfigurationFactory.create(configuration);
        return new MessageSubscriberProcessor<>(
                channelConfiguration,
                clientRegistry.lookup(channelConfiguration.datasource()));
    }

}
