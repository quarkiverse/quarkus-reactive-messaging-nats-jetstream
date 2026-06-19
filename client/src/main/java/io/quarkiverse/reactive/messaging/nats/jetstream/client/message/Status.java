package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

public interface Status {

    String message();

    int code();

    boolean isError();
}
