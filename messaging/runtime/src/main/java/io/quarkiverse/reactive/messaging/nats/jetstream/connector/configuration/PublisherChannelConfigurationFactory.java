package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import org.eclipse.microprofile.config.Config;
import org.jspecify.annotations.NonNull;

/**
 * Factory interface for creating instances of {@code PublisherChannelConfiguration}.
 * This interface provides a method to construct a {@code PublisherChannelConfiguration}
 * using a provided {@code Config} object.
 */
public interface PublisherChannelConfigurationFactory {

    /**
     * Creates a new instance of {@code PublisherChannelConfiguration} using the provided configuration.
     *
     * @param config the configuration object used to create the {@code PublisherChannelConfiguration}
     * @return an instance of {@code PublisherChannelConfiguration} configured according to the provided {@code config}
     */
    @NonNull
    PublisherChannelConfiguration create(@NonNull Config config);

    /**
     * Creates a new instance of {@code PublisherChannelConfiguration} using the provided name and configuration.
     *
     * @param name the name associated with the publisher channel configuration; must not be null
     * @param config the configuration object used to create the {@code PublisherChannelConfiguration}; must not be null
     * @return an instance of {@code PublisherChannelConfiguration} configured based on the provided parameters
     */
    @NonNull
    PublisherChannelConfiguration create(@NonNull String name, @NonNull Config config);

}
