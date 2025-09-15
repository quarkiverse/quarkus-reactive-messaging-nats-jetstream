package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import static io.nats.client.support.NatsJetStreamConstants.MSG_ID_HDR;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.nats.client.Message;
import io.nats.client.api.MessageInfo;
import io.nats.client.impl.Headers;

public record SubscribeMessageMetadata(String stream,
        String subject,
        String messageId,
        byte[] payload,
        Map<String, List<String>> headers,
        Long deliveredCount,
        String consumer,
        Long streamSequence,
        Long consumerSequence,
        ZonedDateTime timestamp) {

    public static SubscribeMessageMetadata of(Message message) {
        final var headers = Optional.ofNullable(message.getHeaders());
        return SubscribeMessageMetadata.builder()
                .stream(message.metaData().getStream())
                .subject(message.getSubject())
                .payload(message.getData())
                .messageId(headers.map(h -> h.getFirst(MSG_ID_HDR)).orElse(null))
                .headers(headers.map(SubscribeMessageMetadata::headers).orElseGet(HashMap::new))
                .deliveredCount(message.metaData().deliveredCount())
                .consumer(message.metaData().getConsumer())
                .streamSequence(message.metaData().streamSequence())
                .consumerSequence(message.metaData().consumerSequence())
                .timestamp(message.metaData().timestamp())
                .build();
    }

    public static SubscribeMessageMetadata of(MessageInfo message) {
        final var headers = Optional.ofNullable(message.getHeaders());
        return SubscribeMessageMetadata.builder()
                .stream(message.getStream())
                .subject(message.getSubject())
                .payload(message.getData())
                .messageId(headers.map(h -> h.getFirst(MSG_ID_HDR)).orElse(null))
                .headers(headers.map(SubscribeMessageMetadata::headers).orElseGet(HashMap::new))
                .deliveredCount(null)
                .consumer(null)
                .streamSequence(message.getSeq())
                .consumerSequence(null)
                .timestamp(message.getTime())
                .build();
    }

    public static Map<String, List<String>> headers(Headers messageHeaders) {
        final var headers = new HashMap<String, List<String>>();
        messageHeaders.entrySet().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
        return headers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String stream;
        private String subject;
        private String messageId;
        private byte[] payload;
        private Map<String, List<String>> headers;
        private Long deliveredCount;
        private String consumer;
        private Long streamSequence;
        private Long consumerSequence;
        private ZonedDateTime timestamp;

        public SubscribeMessageMetadata build() {
            return new SubscribeMessageMetadata(stream, subject, messageId, payload, headers, deliveredCount, consumer,
                    streamSequence, consumerSequence, timestamp);
        }

        public Builder stream(String stream) {
            this.stream = stream;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder payload(byte[] payload) {
            this.payload = payload;
            return this;
        }

        public Builder headers(Map<String, List<String>> headers) {
            this.headers = headers;
            return this;
        }

        public Builder deliveredCount(Long deliveredCount) {
            this.deliveredCount = deliveredCount;
            return this;
        }

        public Builder consumer(String consumer) {
            this.consumer = consumer;
            return this;
        }

        public Builder streamSequence(Long streamSequence) {
            this.streamSequence = streamSequence;
            return this;
        }

        public Builder consumerSequence(Long consumerSequence) {
            this.consumerSequence = consumerSequence;
            return this;
        }

        public Builder timestamp(ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
    }
}
