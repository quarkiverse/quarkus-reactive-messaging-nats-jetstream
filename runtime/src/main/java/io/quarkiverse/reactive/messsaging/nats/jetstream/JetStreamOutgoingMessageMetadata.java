package io.quarkiverse.reactive.messsaging.nats.jetstream;

import java.util.List;
import java.util.Map;

public class JetStreamOutgoingMessageMetadata {
    private final String messageId;
    private final Map<String, List<String>> headers;

    public JetStreamOutgoingMessageMetadata(final String messageId, final Map<String, List<String>> headers) {
        this.messageId = messageId;
        this.headers = headers;
    }

    public String messageId() {
        return messageId;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }
}
