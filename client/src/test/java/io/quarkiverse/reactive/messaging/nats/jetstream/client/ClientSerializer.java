package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;

public class ClientSerializer implements Serializer {

    @Override
    public <T> T readValue(byte[] data, Class<T> type) {
        final var objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(data, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> byte[] toBytes(T payload) {
        final var objectMapper = new ObjectMapper();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            objectMapper.writeValue(outputStream, payload);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
