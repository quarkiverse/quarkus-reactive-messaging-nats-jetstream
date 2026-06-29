package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.subscriber;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectorConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
@ApplicationScoped
public class MessageSubscriberProcessorFactory {
    private final ConnectorConfiguration configuration;

    public <T> MessageSubscriberProcessor<T> create(String channel, String stream, String subject, Duration retryBackoff) {
        var streamName = Optional.ofNullable(configuration.streams())
                .flatMap(streams -> Optional.ofNullable(streams.get(stream)))
                .flatMap(Stream::name)
                .orElse(stream);
        return new MessageSubscriberProcessor<>(channel, streamName, subject, client, retryBackoff);
    }

}
