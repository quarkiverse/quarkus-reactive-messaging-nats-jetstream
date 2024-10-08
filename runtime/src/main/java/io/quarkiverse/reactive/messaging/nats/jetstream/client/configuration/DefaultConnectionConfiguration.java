package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
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
    public String servers() {
        return configuration.servers();
    }

    @Override
    public Optional<String> password() {
        return configuration.password();
    }

    @Override
    public Optional<String> username() {
        return configuration.username();
    }

    @Override
    public Optional<String> token() {
        return configuration.token();
    }

    @Override
    public Optional<String> credentialPath() {
        return configuration.credentialPath();
    }

    @Override
    public boolean sslEnabled() {
        return configuration.sslEnabled();
    }

    @Override
    public Optional<Integer> bufferSize() {
        return configuration.bufferSize();
    }

    @Override
    public Optional<ErrorListener> errorListener() {
        return configuration.errorListener().map(this::getInstanceOfErrorListener);
    }

    @Override
    public Optional<Long> connectionTimeout() {
        return configuration.connectionTimeout();
    }

    @Override
    public Optional<String> keystorePath() {
        return configuration.keystorePath();
    }

    @Override
    public Optional<String> keystorePassword() {
        return configuration.keystorePassword();
    }

    @Override
    public Optional<String> truststorePath() {
        return configuration.truststorePath();
    }

    @Override
    public Optional<String> truststorePassword() {
        return configuration.truststorePassword();
    }

    @Override
    public Optional<String> tlsAlgorithm() {
        return configuration.tlsAlgorithm();
    }

    @Override
    public Optional<Duration> connectionBackoff() {
        return configuration.connectionBackoff();
    }

    @Override
    public Optional<Long> connectionAttempts() {
        return configuration.connectionAttempts();
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
