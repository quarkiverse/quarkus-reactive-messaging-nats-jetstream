package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.mutiny.Uni;

public interface Tracer {

    Uni<Message<?>> withTrace(Message<?> message, TraceSupplier traceSupplier);

}
