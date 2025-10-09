package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

public class MessageNotDeletedException extends RuntimeException {

    public MessageNotDeletedException(String stream, long sequence) {
        super(String.format("Unable to delete message in stream %s with sequence %d", stream, sequence));
    }
}
