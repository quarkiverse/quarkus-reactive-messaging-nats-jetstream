package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JetStreamOutgoingMessageMetadata {
    private final String messageId;
    private final Map<String, List<String>> headers;
    private final String subtopic;

    public JetStreamOutgoingMessageMetadata(final String messageId,
            final Map<String, List<String>> headers,
            final String subtopic) {
        this.messageId = messageId;
        this.headers = headers != null ? headers : Collections.emptyMap();
        this.subtopic = subtopic;
    }

    public JetStreamOutgoingMessageMetadata(final String messageId,
            final Map<String, List<String>> headers) {
        this(messageId, headers, null);
    }

    public JetStreamOutgoingMessageMetadata(final String messageId) {
        this(messageId, Collections.emptyMap(), null);
    }

    public String messageId() {
        return messageId;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public Optional<String> subtopic() {
        return Optional.ofNullable(subtopic);
    }
}
