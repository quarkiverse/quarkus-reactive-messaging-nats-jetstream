package io.quarkiverse.reactive.messaging.nats.jetstream.mapper;

import java.time.Duration;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.vertx.mutiny.core.Context;

public interface MessageMapper {

    <T> List<Message<T>> of(List<io.nats.client.Message> messages,
            Class<T> payloadType, Context context, Duration timeout,
            List<Duration> backoff);

    <T> Message<T> of(io.nats.client.Message message,
            Class<T> payloadType, Context context, Duration timeout,
            List<Duration> backoff);
}
