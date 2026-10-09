package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class JacksonSerializer implements Serializer {
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
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }
}
