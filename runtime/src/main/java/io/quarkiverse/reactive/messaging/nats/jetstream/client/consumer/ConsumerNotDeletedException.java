package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public class ConsumerNotDeletedException extends RuntimeException {

    public ConsumerNotDeletedException(final String stream, final String consumerName) {
        super(String.format("Consumer with name = %S not deleted in stream = %s", consumerName, stream));
    }
}
