package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.subscriber;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.config.Config;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.PublisherChannelConfigurationFactory;
import io.smallrye.reactive.messaging.providers.helpers.CDIUtils;

@ApplicationScoped
public class MessageSubscriberProcessorFactory {
    private final Instance<Client> clients;
    private final PublisherChannelConfigurationFactory publisherChannelConfigurationFactory;

    public MessageSubscriberProcessorFactory(@Any Instance<Client> clients,
            PublisherChannelConfigurationFactory publisherChannelConfigurationFactory) {
        this.clients = clients;
        this.publisherChannelConfigurationFactory = publisherChannelConfigurationFactory;
    }

    public <T> MessageSubscriberProcessor<T> create(@NonNull final Config configuration) {
        final var channelConfiguration = publisherChannelConfigurationFactory.create(configuration);
        return new MessageSubscriberProcessor<>(
                channelConfiguration,
                CDIUtils.getInstanceById(clients, channelConfiguration.datasource()).get());
    }

}
