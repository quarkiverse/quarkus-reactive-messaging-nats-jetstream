package io.quarkiverse.reactive.nats.jetstream.tracing.message;

public interface MessageAttributes {
    String MESSAGE_STREAM = "message.stream";
    String MESSAGE_SUBJECT = "message.subject";
    String MESSAGE_PAYLOAD = "message.payload";
    String MESSAGE_STREAM_SEQUENCE = "message.stream_sequence";
    String MESSAGE_CONSUMER_SEQUENCE = "message.consumer_sequence";
    String MESSAGE_CONSUMER = "message.consumer";
    String MESSAGE_DELIVERED_COUNT = "message.delivered_count";
    String MESSAGE_ID = "message.id";
    String MESSAGE_TYPE = "message.type";
}
