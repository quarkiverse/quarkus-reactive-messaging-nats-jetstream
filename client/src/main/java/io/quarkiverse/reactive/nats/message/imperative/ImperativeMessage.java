package io.quarkiverse.reactive.nats.message.imperative;

public interface ImperativeMessage extends io.nats.client.Message {

    static ImperativeMessage of(io.nats.client.Message message) {
        return new ImperativeMessageDelegate(message);
    }
}
