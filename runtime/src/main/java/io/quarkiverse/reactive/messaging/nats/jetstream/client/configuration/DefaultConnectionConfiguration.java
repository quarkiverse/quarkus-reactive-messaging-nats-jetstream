package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import static io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper.loadClass;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import io.nats.client.ErrorListener;
import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;

class DefaultConnectionConfiguration implements ConnectionConfiguration {
    private final NatsConfiguration configuration;

    DefaultConnectionConfiguration(NatsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getServers() {
        return configuration.servers();
    }

    @Override
    public Optional<String> getPassword() {
        return configuration.password();
    }

    @Override
    public Optional<String> getUsername() {
        return configuration.username();
    }

    @Override
    public Optional<Integer> getMaxReconnects() {
        return configuration.maxReconnects();
    }

    @Override
    public boolean sslEnabled() {
        return configuration.sslEnabled();
    }

    @Override
    public Optional<Integer> getBufferSize() {
        return configuration.bufferSize();
    }

    @Override
    public Optional<ErrorListener> getErrorListener() {
        return configuration.errorListener().map(this::getInstanceOfErrorListener);
    }

    @Override
    public Optional<Long> getConnectionTimeout() {
        return configuration.connectionTimeout();
    }

    private ErrorListener getInstanceOfErrorListener(String className) {
        try {
            var clazz = loadClass(className);
            var constructor = clazz.getConstructor();
            return (ErrorListener) constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException("Not able to create instance of error listener", e);
        }
    }
}
