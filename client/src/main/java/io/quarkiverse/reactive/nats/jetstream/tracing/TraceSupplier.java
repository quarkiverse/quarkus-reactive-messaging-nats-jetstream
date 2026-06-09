package io.quarkiverse.reactive.nats.jetstream.tracing;

import io.quarkiverse.reactive.nats.jetstream.message.Message;
import org.jspecify.annotations.NonNull;

public interface TraceSupplier {

    @NonNull Message get(@NonNull Message message);

}
