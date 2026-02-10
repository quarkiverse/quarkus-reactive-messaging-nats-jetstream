package io.quarkiverse.reactive.messaging.nats.jetstream.deployment;

public record JetStreamConfiguration(String username, String password, boolean sslEnabled, String certificateFile,
        String keyFile) {

    static JetStreamConfiguration of(JetStreamDevServicesBuildTimeConfiguration configuration) {
        return new JetStreamConfiguration(
                configuration.username(),
                configuration.password(),
                configuration.tlsConfiguration().isPresent(),
                configuration.tlsConfiguration()
                        .map(JetStreamDevServicesBuildTimeConfiguration.TlsConfiguration::certificateFile).orElse(null),
                configuration.tlsConfiguration().map(JetStreamDevServicesBuildTimeConfiguration.TlsConfiguration::keyFile)
                        .orElse(null));
    }
}
