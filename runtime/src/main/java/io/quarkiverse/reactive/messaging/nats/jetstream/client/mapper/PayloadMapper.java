package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.api.MessageInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Payload;
import org.mapstruct.Context;
import org.mapstruct.Mapper;

import java.io.IOException;
import java.util.Optional;

import static io.quarkiverse.reactive.messaging.nats.jetstream.client.api.JetStreamMessage.MESSAGE_TYPE_HEADER;

@Mapper(componentModel = "cdi", uses = PayloadMapper.class)
public interface PayloadMapper {

    <T> Payload<T> map(byte[] data, Class<T> type);

    <T> byte[] map(Payload<T> payload);

    <T> Payload<T> map(MessageInfo message);

    <T> Payload<T> map(io.nats.client.Message message, Class<T> payLoadType);

    <T> Payload<T> map(io.nats.client.Message message);

    default <T> byte[] map(Payload<T> payload, @Context ObjectMapper objectMapper) {
        try {
            if (payload == null) {
                return new byte[0];
            } else if (payload.data() instanceof byte[] bytePayload) {
                return bytePayload;
            } else {
                return objectMapper.writeValueAsBytes(payload.data());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    default <T> Payload<T> map(byte[] data, Class<T> type, @Context ObjectMapper objectMapper) {
        try {
            return Payload.<T>builder().data(objectMapper.readValue(data, type)).type(type).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    default <T> Payload<T> map(MessageInfo message, @Context ObjectMapper objectMapper) {
        return Optional.ofNullable(message).flatMap(m -> Optional.ofNullable(m.getHeaders()))
                .flatMap(headers -> Optional.ofNullable(headers.getFirst(MESSAGE_TYPE_HEADER)))
                .map(PayloadMapper::loadClass)
                .map(type -> Payload.<T>builder()
                        .data((T) map(message.getData(), type, objectMapper).data())
                        .type((Class<T>)type).build())
                .orElse(null);
    }

    default <T> Payload<T> map(io.nats.client.Message message, Class<T> payLoadType, @Context ObjectMapper objectMapper) {
        return map(message.getData(), payLoadType, objectMapper);
    }

    @SuppressWarnings({"unchecked", "DuplicatedCode"})
    default <T> Payload<T> map(io.nats.client.Message message, @Context ObjectMapper objectMapper) {
        return Optional.ofNullable(message).flatMap(m -> Optional.ofNullable(m.getHeaders()))
                .flatMap(headers -> Optional.ofNullable(headers.getFirst(MESSAGE_TYPE_HEADER)))
                .map(PayloadMapper::loadClass)
                .map(type -> Payload.<T>builder()
                        .data((T) map(message.getData(), type, objectMapper).data())
                        .type((Class<T>) type)
                        .build())
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    static <T> Class<T> loadClass(String type) {
        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return (Class<T>) classLoader.loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
