package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.util.Optional;

import io.nats.client.ErrorListener;
import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;

public interface ConnectionConfiguration {

    String getServers();

    Optional<String> getPassword();

    Optional<String> getUsername();

    Optional<String> getToken();

    Optional<Integer> getMaxReconnects();

    boolean sslEnabled();

    Optional<Integer> getBufferSize();

    Optional<ErrorListener> getErrorListener();

    Optional<Long> getConnectionTimeout();

    static ConnectionConfiguration of(NatsConfiguration configuration) {
        return new DefaultConnectionConfiguration(configuration);
    }
}
