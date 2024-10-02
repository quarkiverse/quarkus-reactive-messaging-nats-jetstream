package io.quarkiverse.reactive.messaging.nats.jetstream.mapper;

import static io.quarkiverse.reactive.messaging.nats.jetstream.mapper.DefaultMessageMapper.MESSAGE_TYPE_HEADER;

import java.io.IOException;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.api.MessageInfo;
import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class DefaultPayloadMapper implements PayloadMapper {

    private final ObjectMapper objectMapper;

    public DefaultPayloadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Returns a byte array containing the supplied payload.
     *
     * @param payload the payload
     * @return a byte array encapsulation of the payload
     */
    @Override
    public byte[] of(final Object payload) {
        try {
            if (payload == null) {
                return new byte[0];
            } else if (payload instanceof byte[]) {
                return (byte[]) payload;
            } else {
                return objectMapper.writeValueAsBytes(payload);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T of(byte[] data, Class<T> type) {
        try {
            return objectMapper.readValue(data, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> Optional<? super T> of(io.nats.client.Message message) {
        return Optional.ofNullable(message).flatMap(m -> Optional.ofNullable(m.getHeaders()))
                .flatMap(headers -> Optional.ofNullable(headers.getFirst(MESSAGE_TYPE_HEADER)))
                .map(DefaultPayloadMapper::loadClass)
                .map(type -> of(message.getData(), type));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> of(MessageInfo message) {
        return Optional.ofNullable(message).flatMap(m -> Optional.ofNullable(m.getHeaders()))
                .flatMap(headers -> Optional.ofNullable(headers.getFirst(MESSAGE_TYPE_HEADER)))
                .map(DefaultPayloadMapper::loadClass)
                .map(type -> (T) of(message.getData(), type));
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
