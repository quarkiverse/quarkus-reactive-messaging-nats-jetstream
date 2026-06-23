package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import io.nats.client.JetStreamApiException;
import io.nats.client.api.*;
import io.nats.client.api.ObjectInfo;
import io.nats.client.api.ObjectStoreStatus;
import io.nats.client.impl.NatsObjectStoreWatchSubscription;

public record ObjectStoreDelegate(io.nats.client.ObjectStore delegate) implements ObjectStore {

    @Override
    public String getBucketName() {
        return delegate.getBucketName();
    }

    @Override
    public ObjectInfo put(ObjectMeta meta, InputStream inputStream)
            throws IOException, JetStreamApiException, NoSuchAlgorithmException {
        return delegate.put(meta, inputStream);
    }

    @Override
    public ObjectInfo put(String objectName, InputStream inputStream)
            throws IOException, JetStreamApiException, NoSuchAlgorithmException {
        return delegate.put(objectName, inputStream);
    }

    @Override
    public ObjectInfo put(String objectName, byte[] input) throws IOException, JetStreamApiException, NoSuchAlgorithmException {
        return delegate.put(objectName, input);
    }

    @Override
    public ObjectInfo put(File file) throws IOException, JetStreamApiException, NoSuchAlgorithmException {
        return delegate.put(file);
    }

    @Override
    public ObjectInfo get(String objectName, OutputStream outputStream)
            throws IOException, JetStreamApiException, InterruptedException, NoSuchAlgorithmException {
        return delegate.get(objectName, outputStream);
    }

    @Override
    public ObjectInfo getInfo(String objectName) throws IOException, JetStreamApiException {
        return delegate.getInfo(objectName);
    }

    @Override
    public ObjectInfo getInfo(String objectName, boolean includingDeleted) throws IOException, JetStreamApiException {
        return delegate.getInfo(objectName, includingDeleted);
    }

    @Override
    public ObjectInfo updateMeta(String objectName, ObjectMeta meta) throws IOException, JetStreamApiException {
        return delegate.updateMeta(objectName, meta);
    }

    @Override
    public ObjectInfo delete(String objectName) throws IOException, JetStreamApiException {
        return delegate.delete(objectName);
    }

    @Override
    public ObjectInfo addLink(String objectName, ObjectInfo toInfo) throws IOException, JetStreamApiException {
        return delegate.addLink(objectName, toInfo);
    }

    @Override
    public ObjectInfo addBucketLink(String objectName, io.nats.client.ObjectStore toStore)
            throws IOException, JetStreamApiException {
        return delegate.addBucketLink(objectName, toStore);
    }

    @Override
    public ObjectStoreStatus seal() throws IOException, JetStreamApiException {
        return delegate.seal();
    }

    @Override
    public List<ObjectInfo> getList() throws IOException, JetStreamApiException, InterruptedException {
        return delegate.getList();
    }

    @Override
    public NatsObjectStoreWatchSubscription watch(ObjectStoreWatcher watcher, ObjectStoreWatchOption... watchOptions)
            throws IOException, JetStreamApiException, InterruptedException {
        return delegate.watch(watcher, watchOptions);
    }

    @Override
    public ObjectStoreStatus getStatus() throws IOException, JetStreamApiException {
        return delegate.getStatus();
    }
}
