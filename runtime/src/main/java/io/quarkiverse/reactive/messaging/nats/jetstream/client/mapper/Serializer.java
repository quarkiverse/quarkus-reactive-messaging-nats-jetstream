package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

public interface Serializer {

    <T> T readValue(byte[] data, Class<T> type);

    <T> byte[] toBytes(T payload);

}
