package io.quarkiverse.reactive.nats.message;


import java.time.ZonedDateTime;

public interface MetaData {
    String metaType();
    String domain();
    String stream();
    String consumer();
    long deliveredCount();
    long streamSequence();
    long consumerSequence();
    ZonedDateTime timestamp();
    long pendingCount();
}
