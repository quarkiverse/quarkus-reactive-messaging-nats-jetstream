package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
class MessageMapperImpl implements MessageMapper {
    private final Serializer serializer;

    @Override
    public @NonNull Message<byte[]> map(final org.eclipse.microprofile.reactive.messaging.@NonNull Message<?> message) {
        final var headers = message.getMetadata(PublishHeaders.class).orElseGet(PublishHeaders::of);
        if (message.getPayload() != null) {
            headers.setPayloadType(message.getPayload().getClass());
        }
        final var payload = message.getPayload() != null ? serializer.toBytes(message.getPayload()) : new byte[0];
        return Message.of(payload, message.getMetadata().with(headers));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> org.eclipse.microprofile.reactive.messaging.@NonNull Message<T> map(@NonNull final Message<byte[]> message) {
        return this.<T> deserialize(message).orElseGet(() -> (Message<T>) (Message<?>) message);
    }

    @Override
    public <T> org.eclipse.microprofile.reactive.messaging.@NonNull Message<T> map(@NonNull final Message<byte[]> message,
                                                                                   @NonNull final Class<T> payloadType) {
        return Message.of(message, serializer.readValue(message.getPayload(), payloadType));
    }

    @SuppressWarnings("unchecked")
    private <T> @NonNull Optional<org.eclipse.microprofile.reactive.messaging.Message<T>> deserialize(@NonNull final Message<byte[]> message) {
        return payloadType(message)
                .map(payloadType -> (Class<T>) payloadType)
                .map(payloadType -> Message.of(message, deserialize(message.getPayload(), payloadType)));
    }

    private <T> @Nullable T deserialize(byte @Nullable [] payload, @NonNull Class<T> payloadType) {
        return payload != null ? serializer.readValue(payload, payloadType) : null;
    }

    private <T> @NonNull Optional<Class<T>> payloadType(@NonNull final Message<byte[]> message) {
        return message.getMetadata(MessageHeaders.class).flatMap(this::payloadType);
    }

    private <T> @NonNull Optional<Class<T>> payloadType(@NonNull final Headers headers) {
        return headers.payloadType();
    }
}
