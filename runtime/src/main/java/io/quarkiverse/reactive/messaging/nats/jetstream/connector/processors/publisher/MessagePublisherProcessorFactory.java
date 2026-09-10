package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.publisher;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientRegistry;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConsumerChannelConfigurationFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class MessagePublisherProcessorFactory {
    private final ClientRegistry clientRegistry;
    private final ConsumerChannelConfigurationFactory channelConfigurationFactory;

    public MessagePublisherProcessor<?> create(@NonNull final Config configuration) {
        final var channelConfiguration = channelConfigurationFactory.create(configuration);
        return new MessagePublisherProcessor<>(
                channelConfiguration,
                clientRegistry.lookup(channelConfiguration.datasource()));
    }

}
