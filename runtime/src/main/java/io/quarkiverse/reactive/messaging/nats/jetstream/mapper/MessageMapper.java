package io.quarkiverse.reactive.messaging.nats.jetstream.mapper;

import java.time.Duration;

import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.vertx.mutiny.core.Context;

public interface MessageMapper {

    <T> org.eclipse.microprofile.reactive.messaging.Message<T> of(io.nats.client.Message message,
            boolean tracingEnabled,
            Class<T> payloadType,
            Context context,
            ExponentialBackoff exponentialBackoff,
            Duration ackTimeout);
}
