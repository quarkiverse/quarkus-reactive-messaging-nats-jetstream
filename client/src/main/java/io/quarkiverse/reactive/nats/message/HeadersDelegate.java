package io.quarkiverse.reactive.nats.message;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record HeadersDelegate(io.nats.client.impl.Headers delegate) implements Headers {

    @Override
    public Headers add(String key, String... values) {
        return new HeadersDelegate(delegate.add(key, values));
    }

    @Override
    public Headers add(String key, Collection<String> values) {
        return new HeadersDelegate(delegate.add(key, values));
    }

    @Override
    public Headers put(String key, String... values) {
        return new HeadersDelegate(delegate.put(key, values));
    }

    @Override
    public Headers put(String key, Collection<String> values) {
        return new HeadersDelegate(delegate.put(key, values));
    }

    @Override
    public Headers put(Map<String, List<String>> map) {
        return new HeadersDelegate(delegate.put(map));
    }

    @Override
    public void remove(String... keys) {
        delegate.remove(keys);
    }

    @Override
    public void remove(Collection<String> keys) {
        delegate.remove(keys);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean containsKey(String key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsKeyIgnoreCase(String key) {
        return delegate.containsKeyIgnoreCase(key);
    }

    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    @Override
    public Set<String> keySetIgnoreCase() {
        return delegate.keySetIgnoreCase();
    }

    @Override
    public @Nullable List<String> get(String key) {
        return delegate.get(key);
    }

    @Override
    public @Nullable String getFirst(String key) {
        return delegate.getFirst(key);
    }

    @Override
    public @Nullable String getLast(String key) {
        return delegate.getLast(key);
    }

    @Override
    public @Nullable List<String> getIgnoreCase(String key) {
        return delegate.getIgnoreCase(key);
    }

    @Override
    public @NonNull Set<Map.Entry<String, List<String>>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public boolean isDirty() {
        return delegate.isDirty();
    }

    @Override
    public boolean isReadOnly() {
        return delegate.isReadOnly();
    }
}
