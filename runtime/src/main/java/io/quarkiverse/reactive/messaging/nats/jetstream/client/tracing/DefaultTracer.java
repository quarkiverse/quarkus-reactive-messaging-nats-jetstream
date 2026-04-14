package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.mutiny.Uni;

public class DefaultTracer<T> implements Tracer<T> {

    @Override
    public Uni<Message<T>> withTrace(Message<T> message, TraceSupplier<T> traceSupplier) {
        return Uni.createFrom().item(message);
    }
}
