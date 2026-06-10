package io.quarkiverse.reactive.messaging.nats.jetstream.message;

public interface PayloadMapper<T> {

    byte[] toBytes(Object payload);

    T fromBytes(byte[] bytes, Class<T> type);

}
