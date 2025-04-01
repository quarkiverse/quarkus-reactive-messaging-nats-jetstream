package io.quarkiverse.reactive.messaging.nats.jetstream.mapper;

import java.time.Duration;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.nats.client.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessage;
import io.quarkus.arc.DefaultBean;
import io.vertx.mutiny.core.Context;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@DefaultBean
@RequiredArgsConstructor
public class DefaultMessageMapper implements MessageMapper {
    private final PayloadMapper payloadMapper;

    @SuppressWarnings("unchecked")
    @Override
    public <T> SubscribeMessage<T> of(
            Message message,
            Class<T> payloadType,
            Context context,
            Duration timeout) {
        try {
            return payloadType != null
                    ? new SubscribeMessage<>(message, payloadMapper.of(message, payloadType), context, timeout)
                    : new SubscribeMessage<>(message,
                            (T) payloadMapper.of(message).orElseGet(message::getData),
                            context,
                            timeout);
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> List<SubscribeMessage<T>> of(List<Message> messages, Class<T> payloadType, Context context, Duration timeout) {
        return messages.stream().map(message -> of(message, payloadType, context, timeout)).toList();
    }
}
