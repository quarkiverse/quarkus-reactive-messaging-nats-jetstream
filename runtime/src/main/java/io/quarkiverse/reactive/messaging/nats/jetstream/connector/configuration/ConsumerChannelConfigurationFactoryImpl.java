package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.JetStreamConnectorIncomingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientRegistry;

@ApplicationScoped
public class ConsumerChannelConfigurationFactoryImpl implements ConsumerChannelConfigurationFactory {

    @Override
    public @NonNull ConsumerChannelConfiguration create(@NonNull Config config) {
        final var channelConfig = new JetStreamConnectorIncomingConfiguration(config);
        return ConsumerChannelConfigurationImpl.builder()
                .name(channelConfig.getChannel())
                .stream(channelConfig.getStream()
                        .orElseThrow(() -> new IllegalArgumentException(
                                String.format("Missing stream for channel: %s", channelConfig.getChannel()))))
                .retryBackoff(channelConfig.getRetryBackoff().map(Duration::ofMillis))
                .datasource(channelConfig.getDatasource().orElse(ClientRegistry.DEFAULT_CLIENT_NAME))
                .consumer(channelConfig.getConsumer())
                .batchSize(channelConfig.getBatchSize())
                .timeout(Duration.ofMillis(channelConfig.getTimeout()))
                .payloadType(channelConfig.getPayloadType().map(this::loadClass))
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> loadClass(String type) {
        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return (Class<T>) classLoader.loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
