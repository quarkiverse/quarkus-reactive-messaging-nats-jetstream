package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

public class BucketNotFoundException extends RuntimeException {

    public BucketNotFoundException(String bucketName) {
        super(String.format("Bucket %s not found", bucketName));
    }
}
