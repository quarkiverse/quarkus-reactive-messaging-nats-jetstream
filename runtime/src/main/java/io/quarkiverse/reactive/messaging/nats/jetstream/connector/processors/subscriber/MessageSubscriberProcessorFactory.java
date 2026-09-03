package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.subscriber;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.JetStreamConnectorOutgoingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class MessageSubscriberProcessorFactory {
    private final ClientRegistry clientRegistry;

    public <T> MessageSubscriberProcessor<T> create(@NonNull final JetStreamConnectorOutgoingConfiguration configuration) {
        final var channel = configuration.getChannel();
        final var stream = configuration.getStream().orElseThrow(
                () -> new IllegalArgumentException("The 'stream' attribute must be set for the JetStream connector."));
        final var subject = configuration.getSubject().orElseThrow(
                () -> new IllegalArgumentException("The 'subject' attribute must be set for the JetStream connector."));
        final var datasource = configuration.getDatasource().orElse(null);
        final var retryBackoff = Duration.ofMillis(configuration.getRetryBackoff());
        return new MessageSubscriberProcessor<>(
                channel,
                stream,
                subject,
                datasource != null ? clientRegistry.lookup(datasource)
                        : clientRegistry.lookup(ClientRegistry.DEFAULT_CLIENT_NAME),
                retryBackoff);
    }

}
