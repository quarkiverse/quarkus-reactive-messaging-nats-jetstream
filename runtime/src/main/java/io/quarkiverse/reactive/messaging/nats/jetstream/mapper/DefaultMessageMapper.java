package io.quarkiverse.reactive.messaging.nats.jetstream.mapper;

import java.time.Duration;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Message;

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
    public <T> Message<T> of(
            io.nats.client.Message message,
            Class<T> payloadType,
            Context context,
            Duration timeout,
            List<Duration> backoff) {
        try {
            return payloadType != null
                    ? new SubscribeMessage<>(message, payloadMapper.of(message, payloadType), context, timeout, backoff)
                    : new SubscribeMessage<>(message,
                            (T) payloadMapper.of(message).orElseGet(message::getData),
                            context,
                            timeout,
                            backoff);
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> List<Message<T>> of(List<io.nats.client.Message> messages, Class<T> payloadType, Context context,
            Duration timeout,
            List<Duration> backoff) {
        return messages.stream().map(message -> of(message, payloadType, context, timeout, backoff)).toList();
    }
}
