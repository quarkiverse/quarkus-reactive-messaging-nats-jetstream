package io.quarkiverse.reactive.messaging.nats.jetstream.mapper;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessage;
import io.quarkus.arc.DefaultBean;
import io.vertx.mutiny.core.Context;

@ApplicationScoped
@DefaultBean
public class DefaultMessageMapper implements MessageMapper {
    private final PayloadMapper payloadMapper;

    @Inject
    public DefaultMessageMapper(PayloadMapper payloadMapper) {
        this.payloadMapper = payloadMapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> PublishMessage<T> of(
            io.nats.client.Message message,
            Class<T> payloadType,
            Context context,
            ExponentialBackoff exponentialBackoff,
            Duration ackTimeout) {
        try {
            return payloadType != null
                    ? new PublishMessage<>(message, payloadMapper.of(message, payloadType), context,
                            exponentialBackoff, ackTimeout)
                    : new PublishMessage<>(message,
                            (T) payloadMapper.of(message).orElseGet(message::getData),
                            context,
                            exponentialBackoff, ackTimeout);
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }
}
