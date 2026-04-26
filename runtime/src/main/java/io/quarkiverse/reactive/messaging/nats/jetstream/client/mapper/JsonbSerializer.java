package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class JsonbSerializer implements Serializer {
    private final Jsonb jsonb;

    public JsonbSerializer() {
        this.jsonb = JsonbBuilder.create();
    }

    @Override
    public <T> T readValue(byte[] data, Class<T> type) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data)) {
            return jsonb.fromJson(byteArrayInputStream, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> byte[] toBytes(T payload) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            jsonb.toJson(payload, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
