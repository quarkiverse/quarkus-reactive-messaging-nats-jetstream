package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class KeyValueNotFoundException extends RuntimeException {

    public KeyValueNotFoundException(String bucketName, String key) {
        super(String.format("Key %s not found in bucket %s", key, bucketName));
    }
}
