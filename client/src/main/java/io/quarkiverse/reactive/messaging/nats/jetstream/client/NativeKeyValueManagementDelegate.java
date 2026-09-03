package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.io.IOException;
import java.util.List;

import io.nats.client.JetStreamApiException;
import io.nats.client.api.KeyValueConfiguration;
import io.nats.client.api.KeyValueStatus;

record NativeKeyValueManagementDelegate(io.nats.client.KeyValueManagement delegate) implements NativeKeyValueManagement {

    @Override
    public KeyValueStatus create(KeyValueConfiguration config) throws IOException, JetStreamApiException {
        return delegate.create(config);
    }

    @Override
    public KeyValueStatus update(KeyValueConfiguration config) throws IOException, JetStreamApiException {
        return delegate.update(config);
    }

    @Override
    public List<String> getBucketNames() throws IOException, JetStreamApiException {
        return delegate.getBucketNames();
    }

    @SuppressWarnings("deprecation")
    @Override
    public KeyValueStatus getBucketInfo(String bucketName) throws IOException, JetStreamApiException {
        return delegate.getBucketInfo(bucketName);
    }

    @Override
    public KeyValueStatus getStatus(String bucketName) throws IOException, JetStreamApiException {
        return delegate.getStatus(bucketName);
    }

    @Override
    public List<KeyValueStatus> getStatuses() throws IOException, JetStreamApiException {
        return delegate.getStatuses();
    }

    @Override
    public void delete(String bucketName) throws IOException, JetStreamApiException {
        delegate.delete(bucketName);
    }
}
