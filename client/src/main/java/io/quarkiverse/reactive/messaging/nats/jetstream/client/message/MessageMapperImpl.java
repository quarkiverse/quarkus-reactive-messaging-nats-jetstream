package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class MessageMapperImpl implements MessageMapper {
    private final Serializer serializer;

    @Override
    public Message map(final org.eclipse.microprofile.reactive.messaging.@NonNull Message<?> message) {
        final var headers = message.getMetadata(PublishHeaders.class).orElseGet(PublishHeaders::of);
        if (message.getPayload() != null) {
            headers.setPayloadType(message.getPayload().getClass());
        }
        final var payload = message.getPayload() != null ? serializer.toBytes(message.getPayload()) : new byte[0];
        return Message.of(payload, message.getMetadata().with(headers));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> org.eclipse.microprofile.reactive.messaging.Message<T> map(@NonNull final Message message) {
        final var metadata = message.getMetadata();
        if (message.getPayload() != null) {
            return (org.eclipse.microprofile.reactive.messaging.Message<T>) deserialize(message)
                    .orElseGet(() -> org.eclipse.microprofile.reactive.messaging.Message.of(message.getPayload(), metadata));

        }
        return org.eclipse.microprofile.reactive.messaging.Message.of(null, metadata);
    }

    @Override
    public <T> org.eclipse.microprofile.reactive.messaging.Message<T> map(@NonNull final Message message,
            @NonNull final Class<T> payloadType) {
        return org.eclipse.microprofile.reactive.messaging.Message.of(serializer.readValue(message.getPayload(), payloadType),
                message.getMetadata());
    }

    private <T> Optional<org.eclipse.microprofile.reactive.messaging.Message<T>> deserialize(@NonNull final Message message) {
        return Optional.ofNullable(message.getPayload()).flatMap(payload -> deserialize(message, payload));
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<org.eclipse.microprofile.reactive.messaging.Message<T>> deserialize(@NonNull final Message message,
            byte @NonNull [] payload) {
        return payloadType(message)
                .map(payloadType -> (org.eclipse.microprofile.reactive.messaging.Message<T>) deserialize(payload, payloadType,
                        message.getMetadata()));
    }

    private <T> org.eclipse.microprofile.reactive.messaging.Message<T> deserialize(byte[] payload, Class<T> payloadType,
            org.eclipse.microprofile.reactive.messaging.Metadata metadata) {
        return org.eclipse.microprofile.reactive.messaging.Message.of(
                serializer.readValue(payload, payloadType),
                metadata);
    }

    private <T> Optional<Class<T>> payloadType(@NonNull final Message message) {
        return message.getMetadata(MessageHeaders.class).flatMap(this::payloadType);
    }

    private <T> Optional<Class<T>> payloadType(@NonNull final Headers headers) {
        return headers.payloadType();
    }
}
