package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.publisher;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.config.Config;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConsumerChannelConfigurationFactory;
import io.smallrye.reactive.messaging.providers.helpers.CDIUtils;

@ApplicationScoped
public class MessagePublisherProcessorFactory {
    private final Instance<Client> clients;
    private final ConsumerChannelConfigurationFactory channelConfigurationFactory;

    public MessagePublisherProcessorFactory(@Any Instance<Client> clients,
            ConsumerChannelConfigurationFactory channelConfigurationFactory) {
        this.clients = clients;
        this.channelConfigurationFactory = channelConfigurationFactory;
    }

    public MessagePublisherProcessor<?> create(@NonNull final Config configuration) {
        final var channelConfiguration = channelConfigurationFactory.create(configuration);
        return new MessagePublisherProcessor<>(
                channelConfiguration,
                CDIUtils.getInstanceById(clients, channelConfiguration.datasource()).get());
    }

}
