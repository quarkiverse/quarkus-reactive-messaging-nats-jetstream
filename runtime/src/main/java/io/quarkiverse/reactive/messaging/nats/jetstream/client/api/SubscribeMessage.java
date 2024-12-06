package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.util.*;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PublishConfiguration;
import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import lombok.Builder;

@Builder
public record SubscribeMessage<T>(byte[] payload, String type, PublishConfiguration configuration, String messageId,
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
        final var metadata = message.getMetadata(SubscribeMessageMetadata.class).orElse(null);
        final var type = message.getPayload() != null ? message.getPayload().getClass().getTypeName() : null;
        final var headers = metadata != null && metadata.headers() != null ? new HashMap<>(metadata.headers())
                : new HashMap<String, List<String>>();
        if (type != null) {
            headers.put(MESSAGE_TYPE_HEADER, List.of(type));
        }

        return SubscribeMessage.<T> builder()
                .payload(payload)
                .message(message)
                .type(type)
                .configuration(configuration)
                .messageId(
                        metadata != null && metadata.messageId() != null ? metadata.messageId() : UUID.randomUUID().toString())
                .headers(headers)
                .build();
    }

}
