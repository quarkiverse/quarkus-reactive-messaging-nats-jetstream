package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import io.nats.client.JetStreamApiException;
import io.nats.client.MessageTtl;
import io.nats.client.api.*;
import io.nats.client.api.KeyValueEntry;
import io.nats.client.api.KeyValueStatus;
import io.nats.client.impl.NatsKeyValueWatchSubscription;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public record KeyValueDelegate(io.nats.client.KeyValue delegate) implements KeyValue {

    @Override
    public String getBucketName() {
        return delegate.getBucketName();
    }

    @Override
    public KeyValueEntry get(String key) throws IOException, JetStreamApiException {
        return delegate.get(key);
    }

    @Override
    public KeyValueEntry get(String key, long revision) throws IOException, JetStreamApiException {
        return delegate.get(key, revision);
    }

    @Override
    public long put(String key, byte[] value) throws IOException, JetStreamApiException {
        return delegate.put(key, value);
    }

    @Override
    public long put(String key, String value) throws IOException, JetStreamApiException {
        return delegate.put(key, value);
    }

    @Override
    public long put(String key, Number value) throws IOException, JetStreamApiException {
        return delegate.put(key, value);
    }

    @Override
    public long create(String key, byte[] value) throws IOException, JetStreamApiException {
        return delegate.create(key, value);
    }

    @Override
    public long create(String key, byte[] value, MessageTtl messageTtl) throws IOException, JetStreamApiException {
        return delegate.create(key, value, messageTtl);
    }

    @Override
    public long update(String key, byte[] value, long expectedRevision) throws IOException, JetStreamApiException {
        return delegate.update(key, value, expectedRevision);
    }

    @Override
    public long update(String key, String value, long expectedRevision) throws IOException, JetStreamApiException {
        return delegate.update(key, value, expectedRevision);
    }

    @Override
    public void delete(String key) throws IOException, JetStreamApiException {
        delegate.delete(key);
    }

    @Override
    public void delete(String key, long expectedRevision) throws IOException, JetStreamApiException {
        delegate.delete(key, expectedRevision);
    }

    @Override
    public void purge(String key) throws IOException, JetStreamApiException {
        delegate.purge(key);
    }

    @Override
    public void purge(String key, long expectedRevision) throws IOException, JetStreamApiException {
        delegate.purge(key, expectedRevision);
    }

    @Override
    public void purge(String key, MessageTtl messageTtl) throws IOException, JetStreamApiException {
        delegate.purge(key, messageTtl);
    }

    @Override
    public void purge(String key, long expectedRevision, MessageTtl messageTtl) throws IOException, JetStreamApiException {
        delegate.purge(key, expectedRevision, messageTtl);
    }

    @Override
    public NatsKeyValueWatchSubscription watch(String key, KeyValueWatcher watcher, KeyValueWatchOption... watchOptions) throws IOException, JetStreamApiException, InterruptedException {
        return delegate.watch(key, watcher, watchOptions);
    }

    @Override
    public NatsKeyValueWatchSubscription watch(String key, KeyValueWatcher watcher, long fromRevision, KeyValueWatchOption... watchOptions) throws IOException, JetStreamApiException, InterruptedException {
        return delegate.watch(key, watcher, fromRevision, watchOptions);
    }

    @Override
    public NatsKeyValueWatchSubscription watch(List<String> keys, KeyValueWatcher watcher, KeyValueWatchOption... watchOptions) throws IOException, JetStreamApiException, InterruptedException {
        return delegate.watch(keys, watcher, watchOptions);
    }

    @Override
    public NatsKeyValueWatchSubscription watch(List<String> keys, KeyValueWatcher watcher, long fromRevision, KeyValueWatchOption... watchOptions) throws IOException, JetStreamApiException, InterruptedException {
        return delegate.watch(keys, watcher, fromRevision, watchOptions);
    }

    @Override
    public NatsKeyValueWatchSubscription watchAll(KeyValueWatcher watcher, KeyValueWatchOption... watchOptions) throws IOException, JetStreamApiException, InterruptedException {
        return delegate.watchAll(watcher, watchOptions);
    }

    @Override
    public NatsKeyValueWatchSubscription watchAll(KeyValueWatcher watcher, long fromRevision, KeyValueWatchOption... watchOptions) throws IOException, JetStreamApiException, InterruptedException {
        return delegate.watchAll(watcher, fromRevision, watchOptions);
    }

    @Override
    public List<String> keys() throws IOException, JetStreamApiException, InterruptedException {
        return delegate.keys();
    }

    @Override
    public List<String> keys(String filter) throws IOException, JetStreamApiException, InterruptedException {
        return delegate.keys(filter);
    }

    @Override
    public List<String> keys(List<String> filters) throws IOException, JetStreamApiException, InterruptedException {
        return delegate.keys(filters);
    }

    @Override
    public LinkedBlockingQueue<KeyResult> consumeKeys() {
        return delegate.consumeKeys();
    }

    @Override
    public LinkedBlockingQueue<KeyResult> consumeKeys(String filter) {
        return delegate.consumeKeys(filter);
    }

    @Override
    public LinkedBlockingQueue<KeyResult> consumeKeys(List<String> filters) {
        return delegate.consumeKeys(filters);
    }

    @Override
    public List<KeyValueEntry> history(String key) throws IOException, JetStreamApiException, InterruptedException {
        return delegate.history(key);
    }

    @Override
    public void purgeDeletes() throws IOException, JetStreamApiException, InterruptedException {
        delegate.purgeDeletes();
    }

    @Override
    public void purgeDeletes(KeyValuePurgeOptions options) throws IOException, JetStreamApiException, InterruptedException {
        delegate.purgeDeletes(options);
    }

    @Override
    public KeyValueStatus getStatus() throws IOException, JetStreamApiException, InterruptedException {
        return delegate.getStatus();
    }
}
