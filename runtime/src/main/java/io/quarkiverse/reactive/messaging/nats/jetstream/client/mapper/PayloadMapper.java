package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import java.util.Optional;

import io.nats.client.api.MessageInfo;

public interface PayloadMapper {

    byte[] of(Object payload);

    <T> T of(byte[] data, Class<T> type);

    <T> Optional<T> of(io.nats.client.Message message);

    <T> Optional<T> of(MessageInfo message);

    default <T> T of(io.nats.client.Message message, Class<T> payLoadType) {
        return of(message.getData(), payLoadType);
    }
}
