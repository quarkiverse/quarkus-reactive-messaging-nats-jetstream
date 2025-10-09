package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public class ConsumerNotResumedException extends RuntimeException {

    public ConsumerNotResumedException(String stream, String consumer) {
        super(String.format("Consumer with name = %s is not resumed in stream = %s", consumer, stream));
    }
}
