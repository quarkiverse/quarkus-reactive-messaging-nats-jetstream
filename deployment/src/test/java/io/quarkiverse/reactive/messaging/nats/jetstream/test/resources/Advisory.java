package io.quarkiverse.reactive.messaging.nats.jetstream.test.resources;

public record Advisory(String type,
        String id,
        String timestamp,
        String stream,
        String consumer,
        long stream_seq,
        long deliveries) {
}
