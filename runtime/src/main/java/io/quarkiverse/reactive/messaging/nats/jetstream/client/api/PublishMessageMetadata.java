package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import static io.quarkiverse.reactive.messaging.nats.jetstream.client.api.JetStreamMessage.MESSAGE_TYPE_HEADER;

import java.util.*;

import org.eclipse.microprofile.reactive.messaging.Message;

public record PublishMessageMetadata(String stream,
        String subject,
        byte[] payload,
        Class<?> type,
        String messageId,
        Map<String, List<String>> headers) {

    public Optional<String> streamOptional() {
        return Optional.ofNullable(stream);
    }

    public Optional<String> subjectOptional() {
        return Optional.ofNullable(subject);
    }

    public Optional<String> messageIdOptional() {
        return Optional.ofNullable(messageId);
    }

    public Optional<Map<String, List<String>>> headersOptional() {
        return Optional.ofNullable(headers);
    }

    public Optional<byte[]> payloadOptional() {
        return Optional.ofNullable(payload);
    }

    public Optional<Class<?>> typeOptional() {
        return Optional.ofNullable(type);
    }

    public static PublishMessageMetadata of(final String messageId,
            final Map<String, List<String>> headers) {
        return PublishMessageMetadata.builder()
                .messageId(messageId)
                .headers(headers)
                .build();
    }

    public static PublishMessageMetadata of(final String messageId) {
        return PublishMessageMetadata.builder()
                .messageId(messageId)
                .build();
    }

    public static <P> PublishMessageMetadata of(final Message<P> message, final String stream,
            final String subject,
            byte[] payload) {
        final var metadata = getMetadata(message);
        final var type = metadata.flatMap(PublishMessageMetadata::typeOptional).orElseGet(() -> getType(message));
        final var headers = metadata.flatMap(PublishMessageMetadata::headersOptional).map(HashMap::new).orElseGet(HashMap::new);
        if (type != null) {
            headers.putIfAbsent(MESSAGE_TYPE_HEADER, List.of(type.getTypeName()));
        }
        return PublishMessageMetadata.builder()
                .stream(stream)
                .subject(metadata.flatMap(PublishMessageMetadata::subjectOptional).orElse(subject))
                .payload(metadata.flatMap(PublishMessageMetadata::payloadOptional).orElse(payload))
                .type(type)
                .messageId(metadata.flatMap(PublishMessageMetadata::messageIdOptional)
                        .orElseGet(() -> UUID.randomUUID().toString()))
                .headers(headers)
                .build();
    }

    private static <P> Optional<PublishMessageMetadata> getMetadata(Message<P> message) {
        return message.getMetadata(PublishMessageMetadata.class);
    }

    @SuppressWarnings("unchecked")
    private static <P> Class<P> getType(Message<P> message) {
        return message.getPayload() != null ? (Class<P>) message.getPayload().getClass() : null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String stream;
        private String subject;
        private byte[] payload;
        private Class<?> type;
        private String messageId;
        private Map<String, List<String>> headers;

        public Builder stream(String stream) {
            this.stream = stream;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder payload(byte[] payload) {
            this.payload = payload;
            return this;
        }

        public Builder type(Class<?> type) {
            this.type = type;
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder headers(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        public PublishMessageMetadata build() {
            return new PublishMessageMetadata(stream, subject, payload, type, messageId, headers);
        }
    }
}
