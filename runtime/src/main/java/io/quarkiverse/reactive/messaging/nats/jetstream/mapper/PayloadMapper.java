package io.quarkiverse.reactive.messaging.nats.jetstream.mapper;

import java.io.IOException;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.api.MessageInfo;

@ApplicationScoped
public class PayloadMapper {
    private final static Logger logger = Logger.getLogger(PayloadMapper.class);

    public static final String MESSAGE_TYPE_HEADER = "message.type";

    private final ObjectMapper objectMapper;

    @Inject
    public PayloadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Returns a byte array containing the supplied payload.
     *
     * @param payload the payload
     * @return a byte array encapsulation of the payload
     */
    public byte[] toByteArray(final Object payload) {
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

    public <T> Optional<? super T> toPayload(io.nats.client.Message message) {
        return Optional.ofNullable(message).flatMap(m -> Optional.ofNullable(m.getHeaders()))
                .flatMap(headers -> Optional.ofNullable(headers.getFirst(MESSAGE_TYPE_HEADER)))
                .map(PayloadMapper::loadClass)
                .map(type -> decode(message.getData(), type));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> toPayload(MessageInfo message) {
        logger.infof("Getting payload from message info: %s", message);
        return Optional.ofNullable(message).flatMap(m -> Optional.ofNullable(m.getHeaders()))
                .flatMap(headers -> Optional.ofNullable(headers.getFirst(MESSAGE_TYPE_HEADER)))
                .map(PayloadMapper::loadClass)
                .map(type -> (T) decode(message.getData(), type));
    }

    public <T> T toPayload(io.nats.client.Message message, Class<T> payLoadType) {
        return decode(message.getData(), payLoadType);
    }

    private <T> T decode(byte[] data, Class<T> type) {
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
