package io.quarkiverse.reactive.nats.jetstream.message.tracing;

import java.util.Locale;

public enum Operation {
    PUBLISH,
    RECEIVE,
    PROCESS;

    String operationName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
