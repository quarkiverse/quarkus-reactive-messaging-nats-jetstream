package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class ConsumerNotFoundException extends RuntimeException {

    public ConsumerNotFoundException(final String stream, final String consumerName) {
        super(String.format("Consumer with name = %S not found in stream = %s", consumerName, stream));
    }
}
