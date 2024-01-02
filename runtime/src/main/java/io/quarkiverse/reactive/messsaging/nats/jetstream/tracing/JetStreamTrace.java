package io.quarkiverse.reactive.messsaging.nats.jetstream.tracing;

import java.util.List;
import java.util.Map;

import io.quarkiverse.reactive.messsaging.nats.jetstream.JetStreamIncomingMessage;

public class JetStreamTrace {
    private final String stream;
    private final String subject;
    private final String messageId;
    private final Map<String, List<String>> headers;
    private final String payload;

    public JetStreamTrace(String stream, String subject, String messageId, Map<String, List<String>> headers, String payload) {
        this.stream = stream;
        this.subject = subject;
        this.messageId = messageId;
        this.headers = headers;
        this.payload = payload;
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

    public String payload() {
        return payload;
    }

    public static JetStreamTrace trace(JetStreamIncomingMessage<?> message) {
        return new JetStreamTrace(message.getStream(), message.getSubject(), message.getMessageId(), message.getHeaders(),
                new String(message.getData()));
    }
}
