package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.publisher;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.JetStreamConnectorIncomingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class MessagePublisherProcessorFactory {
    private final ClientRegistry clientRegistry;

    public MessagePublisherProcessor<?> create(@NonNull JetStreamConnectorIncomingConfiguration configuration) {
        final var channel = configuration.getChannel();
        final var stream = configuration.getStream().orElseThrow(
                () -> new IllegalArgumentException("The 'stream' attribute must be set for the channel:" + channel));
        final var consumer = configuration.getConsumer().orElseThrow(
                () -> new IllegalArgumentException("The 'consumer' attribute must be set for the channel:" + channel));
        final var datasource = configuration.getDatasource().orElse(null);
        final var batchSize = configuration.getBatchSize();
        final var timeout = Duration.ofMillis(configuration.getTimeout());
        final var retryBackoff = Duration.ofMillis(configuration.getRetryBackoff());
        final var payloadType = configuration.getPayloadType().map(this::loadClass).orElse(null);
        return new MessagePublisherProcessor<>(
                channel,
                stream,
                consumer,
                batchSize,
                timeout,
                datasource != null ? clientRegistry.lookup(datasource)
                        : clientRegistry.lookup(ClientRegistry.DEFAULT_CLIENT_NAME),
                retryBackoff,
                payloadType);
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
