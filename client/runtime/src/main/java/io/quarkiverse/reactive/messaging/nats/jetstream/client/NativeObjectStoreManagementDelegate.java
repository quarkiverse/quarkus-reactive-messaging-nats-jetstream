package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.io.IOException;
import java.util.List;

import io.nats.client.JetStreamApiException;
import io.nats.client.api.ObjectStoreConfiguration;
import io.nats.client.api.ObjectStoreStatus;

record NativeObjectStoreManagementDelegate(
        io.nats.client.ObjectStoreManagement delegate) implements NativeObjectStoreManagement {

    @Override
    public ObjectStoreStatus create(ObjectStoreConfiguration config) throws IOException, JetStreamApiException {
        return delegate.create(config);
    }

    @Override
    public List<String> getBucketNames() throws IOException, JetStreamApiException {
        return delegate.getBucketNames();
    }

    @Override
    public ObjectStoreStatus getStatus(String bucketName) throws IOException, JetStreamApiException {
        return delegate.getStatus(bucketName);
    }

    @Override
    public List<ObjectStoreStatus> getStatuses() throws IOException, JetStreamApiException {
        return delegate.getStatuses();
    }

    @Override
    public void delete(String bucketName) throws IOException, JetStreamApiException {
        delegate.delete(bucketName);
    }
}
