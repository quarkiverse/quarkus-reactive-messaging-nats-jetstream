package io.quarkiverse.reactive.messaging.nats.processors.subscriber;

import java.time.Duration;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.client.Client;
import io.quarkiverse.reactive.messaging.nats.configuration.ConnectorConfiguration;
import io.quarkiverse.reactive.messaging.nats.configuration.Stream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class MessageSubscriberProcessorFactory {
    private final Client client;
    private final ConnectorConfiguration configuration;

    public <T> MessageSubscriberProcessor<T> create(String channel, String stream, String subject, Duration retryBackoff) {
        var streamName = Optional.ofNullable(configuration.streams())
                .flatMap(streams -> Optional.ofNullable(streams.get(stream)))
                .flatMap(Stream::name)
                .orElse(stream);
        return new MessageSubscriberProcessor<>(channel, streamName, subject, client, retryBackoff);
    }

}
