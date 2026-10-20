package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.serializer;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;

public class CustomSerializer implements Serializer {

    @Override
    public <T> T readValue(byte[] data, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> byte[] toBytes(T payload) {
        throw new UnsupportedOperationException();
    }
}
