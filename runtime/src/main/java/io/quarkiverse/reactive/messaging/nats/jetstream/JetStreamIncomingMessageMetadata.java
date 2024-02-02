package io.quarkiverse.reactive.messaging.nats.jetstream;

import static io.nats.client.support.NatsJetStreamConstants.MSG_ID_HDR;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.nats.client.Message;
import io.nats.client.impl.Headers;
import io.smallrye.common.constraint.NotNull;

public class JetStreamIncomingMessageMetadata {
    private final String stream;
    private final String subject;
    private final String messageId;
    private final Map<String, List<String>> headers;
    private final long deliveredCount;

    public JetStreamIncomingMessageMetadata(final String stream,
            final String subject,
            final String messageId,
            final Map<String, List<String>> headers,
            final long deliveredCount) {
        this.stream = stream;
        this.subject = subject;
        this.messageId = messageId;
        this.headers = headers;
        this.deliveredCount = deliveredCount;
    }

    public String stream() {
        return stream;
    }

    public String subject() {
        return subject;
    }

    public String messageId() {
        return messageId;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public long deliveredCount() {
        return deliveredCount;
    }

    public static JetStreamIncomingMessageMetadata create(@NotNull Message message) {
        final var headers = Optional.ofNullable(message.getHeaders());
        return new JetStreamIncomingMessageMetadata(
                message.metaData().getStream(),
                message.getSubject(),
                headers.map(h -> h.getFirst(MSG_ID_HDR)).orElse(null),
                headers.map(JetStreamIncomingMessageMetadata::headers).orElseGet(HashMap::new),
                message.metaData().deliveredCount());
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
