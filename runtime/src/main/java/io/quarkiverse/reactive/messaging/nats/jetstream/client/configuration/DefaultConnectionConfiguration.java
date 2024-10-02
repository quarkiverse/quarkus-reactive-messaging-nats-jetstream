package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import io.nats.client.ErrorListener;
import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.DefaultPayloadMapper;

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
    public Optional<String> getToken() {
        return configuration.token();
    }

    @Override
    public Optional<String> getCredentialPath() {
        return configuration.credentialPath();
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

    @Override
    public Optional<String> getKeystorePath() {
        return configuration.keystorePath();
    }

    @Override
    public Optional<String> getKeystorePassword() {
        return configuration.keystorePassword();
    }

    @Override
    public Optional<String> getTruststorePath() {
        return configuration.truststorePath();
    }

    @Override
    public Optional<String> getTruststorePassword() {
        return configuration.truststorePassword();
    }

    @Override
    public Optional<String> getTlsAlgorithm() {
        return configuration.tlsAlgorithm();
    }

    private ErrorListener getInstanceOfErrorListener(String className) {
        try {
            var clazz = DefaultPayloadMapper.loadClass(className);
            var constructor = clazz.getConstructor();
            return (ErrorListener) constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException("Not able to create instance of error listener", e);
        }
    }
}
