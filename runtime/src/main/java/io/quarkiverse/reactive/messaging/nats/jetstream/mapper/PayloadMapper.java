package io.quarkiverse.reactive.messaging.nats.jetstream.mapper;

import static io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper.MESSAGE_TYPE_HEADER;

import java.io.IOException;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.api.MessageInfo;

@ApplicationScoped
public class PayloadMapper {
    private final ObjectMapper objectMapper;

    public PayloadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Returns a byte array containing the supplied payload.
     *
     * @param payload the payload
     * @return a byte array encapsulation of the payload
     */
    public byte[] of(final Object payload) {
        try {
            if (payload == null) {
                return new byte[0];
            } else if (payload instanceof byte[]) {
                final var byteArray = (byte[]) payload;
                return byteArray;
            } else {
                return objectMapper.writeValueAsBytes(payload);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<? super T> of(io.nats.client.Message message) {
        return Optional.ofNullable(message).flatMap(m -> Optional.ofNullable(m.getHeaders()))
                .flatMap(headers -> Optional.ofNullable(headers.getFirst(MESSAGE_TYPE_HEADER)))
                .map(PayloadMapper::loadClass)
                .map(type -> of(message.getData(), type));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> of(MessageInfo message) {
        return Optional.ofNullable(message).flatMap(m -> Optional.ofNullable(m.getHeaders()))
                .flatMap(headers -> Optional.ofNullable(headers.getFirst(MESSAGE_TYPE_HEADER)))
                .map(PayloadMapper::loadClass)
                .map(type -> (T) of(message.getData(), type));
    }

    public <T> T of(io.nats.client.Message message, Class<T> payLoadType) {
        return of(message.getData(), payLoadType);
    }

    public <T> T of(byte[] data, Class<T> type) {
        try {
            return objectMapper.readValue(data, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(String type) {
        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return (Class<T>) classLoader.loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
