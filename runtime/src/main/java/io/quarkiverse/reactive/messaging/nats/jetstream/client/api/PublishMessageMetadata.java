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
import io.smallrye.common.constraint.NotNull;
import lombok.Builder;

@Builder
public record PublishMessageMetadata(String stream,
        String subject,
        String messageId,
        Map<String, List<String>> headers,
        Long deliveredCount,
        String consumer,
        Long streamSequence,
        Long consumerSequence,
        ZonedDateTime timestamp) {

    public static PublishMessageMetadata of(@NotNull Message message) {
        final var headers = Optional.ofNullable(message.getHeaders());
        return PublishMessageMetadata.builder()
                .stream(message.metaData().getStream())
                .subject(message.getSubject())
                .messageId(headers.map(h -> h.getFirst(MSG_ID_HDR)).orElse(null))
                .headers(headers.map(PublishMessageMetadata::headers).orElseGet(HashMap::new))
                .deliveredCount(message.metaData().deliveredCount())
                .consumer(message.metaData().getConsumer())
                .streamSequence(message.metaData().streamSequence())
                .consumerSequence(message.metaData().consumerSequence())
                .timestamp(message.metaData().timestamp())
                .build();
    }

    public static PublishMessageMetadata of(MessageInfo message) {
        final var headers = Optional.ofNullable(message.getHeaders());
        return PublishMessageMetadata.builder()
                .stream(message.getStream())
                .subject(message.getSubject())
                .messageId(headers.map(h -> h.getFirst(MSG_ID_HDR)).orElse(null))
                .headers(headers.map(PublishMessageMetadata::headers).orElseGet(HashMap::new))
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

    @Override
    public String toString() {
        return "JetStreamIncomingMessageMetadata{" +
                "stream='" + stream + '\'' +
                ", subject='" + subject + '\'' +
                ", messageId='" + messageId + '\'' +
                ", headers=" + headers +
                ", deliveredCount=" + deliveredCount +
                '}';
    }
}
