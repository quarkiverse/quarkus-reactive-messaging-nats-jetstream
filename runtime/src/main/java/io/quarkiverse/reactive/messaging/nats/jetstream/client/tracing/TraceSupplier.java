package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import org.eclipse.microprofile.reactive.messaging.Message;

public interface TraceSupplier {

    Message<?> get(Message<?> message);

}
