package io.quarkiverse.reactive.messaging.nats.jetstream.message;

public interface Status {

    String message();

    int code();

    boolean isError();
}
