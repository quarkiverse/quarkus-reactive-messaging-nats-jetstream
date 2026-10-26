package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import org.eclipse.microprofile.config.Config;
import org.jspecify.annotations.NonNull;

/**
 * A factory interface for creating instances of {@link ConsumerChannelConfiguration}.
 * This interface provides a method to initialize a {@link ConsumerChannelConfiguration}
 * using a specified {@link Config} object, allowing for the customization and
 * configuration of consumer channels.
 */
public interface ConsumerChannelConfigurationFactory {

    /**
     * Creates a new instance of {@link ConsumerChannelConfiguration} using the provided
     * configuration settings.
     *
     * @param config the {@link Config} object containing the configuration properties
     *        required to create a {@link ConsumerChannelConfiguration}
     * @return a new {@link ConsumerChannelConfiguration} instance initialized with the
     *         specified configuration
     */
    @NonNull
    ConsumerChannelConfiguration create(@NonNull Config config);

}
