package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.arc.DefaultBean;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
@DefaultBean
public class SerializerImpl implements Serializer {
    private final ObjectMapper objectMapper;

    @Override
    public <T> T readValue(byte[] data, Class<T> type) {
        try {
            return objectMapper.readValue(data, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> byte[] toBytes(T payload) {
        try {
            if (payload == null) {
                return new byte[0];
            } else if (payload instanceof byte[] bytePayload) {
                return bytePayload;
            } else {
                return objectMapper.writeValueAsBytes(payload);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
