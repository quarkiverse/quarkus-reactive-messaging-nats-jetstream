package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.util.Optional;

public record JetStreamContainerConfigurationImpl(String username, String password, boolean sslEnabled,
        Optional<String> certificateFile,
        Optional<String> keyFile) implements JetStreamContainerConfiguration {
}
