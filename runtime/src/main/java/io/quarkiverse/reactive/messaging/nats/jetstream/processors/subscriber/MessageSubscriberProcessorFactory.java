package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class MessageSubscriberProcessorFactory {
    private final Client client;

    public <T> MessageSubscriberProcessor<T> create(String channel, String stream, String subject, Duration retryBackoff) {
        return new MessageSubscriberProcessor<>(channel, stream, subject, client, retryBackoff);
    }

}
