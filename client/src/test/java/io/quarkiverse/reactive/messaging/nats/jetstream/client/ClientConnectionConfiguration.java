package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamContainer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ErrorListener;

public record ClientConnectionConfiguration(JetStreamContainer jetStreamContainer)
        implements
            io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionConfiguration {

    @Override
    public @NonNull List<String> servers() {
        return List.of(jetStreamContainer.getConnectionInfo());
    }

    @Override
    public @NonNull Optional<String> username() {
        return Optional.of(jetStreamContainer.getConfiguration().username());
    }

    @Override
    public @NonNull Optional<String> password() {
        return Optional.of(jetStreamContainer.getConfiguration().password());
    }

    @Override
    public @NonNull Optional<String> token() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Duration> timeout() {
        return Optional.empty();
    }

    @Override
    public int maximumReconnects() {
        return -1;
    }

    @Override
    public @NonNull Optional<ErrorListener> errorListener() {
        return Optional.of(ErrorListener.of());
    }

    @Override
    public @NonNull Optional<Integer> bufferSize() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<String> tlsAlgorithm() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<SSLContext> sslContext() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<String> credentialPath() {
        return Optional.empty();
    }
}
