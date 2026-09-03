package io.quarkiverse.reactive.messaging.nats.jetstream.connector.client;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ErrorListener;
import lombok.Builder;

@Builder
record ConnectionConfigurationRecord(@NonNull List<String> servers,
        @NonNull Optional<String> username,
        @NonNull Optional<String> password,
        @NonNull Optional<String> token,
        @NonNull Optional<Duration> timeout,
        int maximumReconnects,
        @NonNull Optional<ErrorListener> errorListener,
        @NonNull Optional<Integer> bufferSize,
        @NonNull Optional<String> tlsAlgorithm,
        @NonNull Optional<SSLContext> sslContext,
        @NonNull Optional<String> credentialPath)
        implements
            io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionConfiguration {
}
