package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ErrorListener;

import javax.net.ssl.SSLContext;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface ConnectionConfiguration {
    List<String> servers();

    Optional<String> username();

    Optional<String> password();

    Optional<String> token();

    Optional<Duration> timeout();

    Optional<Integer> maximumReconnects();

    Optional<ErrorListener> errorListener();

    Optional<Integer> bufferSize();

    Optional<String> tlsAlgorithm();

    Optional<SSLContext> sslContext();

    Optional<String> credentialPath();
}
