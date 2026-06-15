package io.quarkiverse.reactive.messaging.nats.jetstream.stream;

public interface StreamContext extends io.nats.client.StreamContext {

    static StreamContext of(io.nats.client.StreamContext delegate) {
        return new StreamContextDelegate(delegate);
    }
}
