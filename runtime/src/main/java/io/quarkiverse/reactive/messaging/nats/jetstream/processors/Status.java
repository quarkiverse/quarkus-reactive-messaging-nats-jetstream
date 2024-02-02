package io.quarkiverse.reactive.messaging.nats.jetstream.processors;

public class Status {
    private final boolean healthy;
    private final String message;

    public Status(boolean healthy, String message) {
        this.healthy = healthy;
        this.message = message;
    }

    public boolean healthy() {
        return healthy;
    }

    public String message() {
        return message;
    }
}
