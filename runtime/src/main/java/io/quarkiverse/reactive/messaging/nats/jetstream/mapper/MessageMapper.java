package io.quarkiverse.reactive.messaging.nats.jetstream.mapper;

import java.util.List;

import io.nats.client.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessage;
import io.vertx.mutiny.core.Context;

public interface MessageMapper {

    <T> List<SubscribeMessage<T>> of(List<Message> messages,
            Class<T> payloadType, Context context);

    <T> SubscribeMessage<T> of(Message message,
            Class<T> payloadType, Context context);
}
