package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public class ConsumerNotFoundException extends RuntimeException {

    public ConsumerNotFoundException(final String stream, final String consumerName) {
        super(String.format("Consumer with name = %S not found in stream = %s", consumerName, stream));
    }

    public ConsumerNotFoundException(final String stream, final String consumerName, Throwable cause) {
        super(String.format("Consumer with name = %S not found in stream = %s", consumerName, stream), cause);
    }
}
