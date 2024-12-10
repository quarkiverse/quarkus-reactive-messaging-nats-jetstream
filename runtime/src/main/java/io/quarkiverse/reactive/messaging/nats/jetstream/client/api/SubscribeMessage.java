package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.util.*;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PublishConfiguration;
import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import lombok.Builder;

@Builder
public record SubscribeMessage<T>(String stream, String subject, byte[] payload, String type, String messageId,
        Map<String, List<String>> headers, Message<T> message) implements JetStreamMessage<T> {

    @Override
    public void injectMetadata(Object o) {
        if (message instanceof MetadataInjectableMessage<T> metadataInjectableMessage) {
            metadataInjectableMessage.injectMetadata(o);
        }
    }

    @Override
    public T getPayload() {
        return message.getPayload();
    }

    public static <T> SubscribeMessage<T> of(Message<T> message, byte[] payload, PublishConfiguration configuration) {
        final var metadata = message.getMetadata(SubscribeMessageMetadata.class);
        final var stream = metadata.flatMap(SubscribeMessageMetadata::streamOptional).orElseGet(configuration::stream);
        final var subject = metadata.flatMap(SubscribeMessageMetadata::subjectOptional).orElseGet(configuration::subject);
        final var messageId = metadata.flatMap(SubscribeMessageMetadata::messageIdOptional)
                .orElseGet(() -> UUID.randomUUID().toString());
        final var type = message.getPayload() != null ? message.getPayload().getClass().getTypeName() : null;
        final var headers = metadata.flatMap(SubscribeMessageMetadata::headersOptional).map(HashMap::new)
                .orElseGet(HashMap::new);
        if (type != null) {
            headers.putIfAbsent(MESSAGE_TYPE_HEADER, List.of(type));
        }
        return SubscribeMessage.<T> builder()
                .stream(stream)
                .subject(subject)
                .payload(payload)
                .message(message)
                .type(type)
                .messageId(messageId)
                .headers(headers)
                .build();
    }

}
