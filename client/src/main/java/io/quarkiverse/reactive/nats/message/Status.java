package io.quarkiverse.reactive.nats.message;

public interface Status {

    int code();

    String message();
}
