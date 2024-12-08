package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PublishConfiguration;
import lombok.Builder;

@Builder
public record Message(String stream,
        String subject,
        String messageId,
        Map<String, List<String>> headers,
        byte[] payload,
        Long streamSequence,
        Long consumerSequence,
        String consumer,
        Long deliveredCount) {

    public static <T> io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamMessage of(PublishMessage<T> message) {
        return io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamMessage.builder()
                .stream(message.getStream())
                .subject(message.getSubject())
                .messageId(message.messageId())
                .headers(message.headers())
                .payload(message.getData())
                .streamSequence(message.getStreamSequence())
                .consumerSequence(message.getConsumerSequence())
                .consumer(message.getConsumer())
                .deliveredCount(message.getDeliveredCount())
                .build();
    }

    public static <T> io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamMessage of(byte[] payload, Class<T> type,
            SubscribeMessageMetadata metadata, PublishConfiguration configuration) {
        final var messageId = metadata != null && metadata.messageId() != null ? metadata.messageId()
                : UUID.randomUUID().toString();
        final var subject = configuration.subject();
        final var headers = new HashMap<String, List<String>>();
        if (type != null) {
            headers.putIfAbsent(JetStreamMessage.MESSAGE_TYPE_HEADER, List.of(type.getTypeName()));
        }
        return io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamMessage.builder()
                .stream(configuration.stream())
                .subject(subject)
                .messageId(messageId)
                .headers(headers)
                .payload(payload)
                .build();
    }
}
