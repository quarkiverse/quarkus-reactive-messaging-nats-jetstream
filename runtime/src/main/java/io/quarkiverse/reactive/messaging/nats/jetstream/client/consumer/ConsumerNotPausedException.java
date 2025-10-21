package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public class ConsumerNotPausedException extends RuntimeException {

    public ConsumerNotPausedException(String stream, String consumer) {
        super(String.format("Consumer with name = %s is not paused in stream = %s", consumer, stream));
    }
}
