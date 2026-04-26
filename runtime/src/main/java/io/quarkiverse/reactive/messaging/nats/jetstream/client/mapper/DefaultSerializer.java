package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import java.io.*;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.DefaultBean;

@ApplicationScoped
@DefaultBean
public class DefaultSerializer implements Serializer {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T readValue(byte[] data, Class<T> type) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> byte[] toBytes(T payload) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(payload);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
