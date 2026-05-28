package io.quarkiverse.reactive.nats.jetstream.message;

public interface Status {

    static Status of(io.nats.client.support.Status delegate) {
        return new StatusDelegate(delegate);
    }

    int code();

    String message();
}
