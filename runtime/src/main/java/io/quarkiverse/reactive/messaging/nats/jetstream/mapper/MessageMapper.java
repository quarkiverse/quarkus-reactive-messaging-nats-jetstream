package io.quarkiverse.reactive.messaging.nats.jetstream.mapper;

import java.time.Duration;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessage;
import io.vertx.mutiny.core.Context;

public interface MessageMapper {

    <T> PublishMessage<T> of(io.nats.client.Message message,
            Class<T> payloadType,
            Context context,
            ExponentialBackoff exponentialBackoff,
            Duration ackTimeout);
}
