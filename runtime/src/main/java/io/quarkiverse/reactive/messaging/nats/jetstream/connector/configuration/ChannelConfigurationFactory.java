package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import org.eclipse.microprofile.config.Config;

public interface ChannelConfigurationFactory {

    PublisherChannelConfiguration create(String channel, Config config);

}
