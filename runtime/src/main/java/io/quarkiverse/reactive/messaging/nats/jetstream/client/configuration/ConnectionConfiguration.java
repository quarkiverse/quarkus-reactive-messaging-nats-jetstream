package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.util.Optional;

import io.nats.client.ErrorListener;
import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;

public interface ConnectionConfiguration {

    String servers();

    Optional<String> password();

    Optional<String> username();

    Optional<String> token();

    boolean sslEnabled();

    Optional<Integer> bufferSize();

    Optional<ErrorListener> errorListener();

    Optional<Long> connectionTimeout();

    Optional<String> credentialPath();

    Optional<String> tlsAlgorithm();

    Optional<Duration> connectionBackoff();

    Optional<Integer> connectionAttempts();

    Optional<String> tlsConfigurationName();

    static ConnectionConfiguration of(NatsConfiguration configuration) {
        return new DefaultConnectionConfiguration(configuration);
    }
}
