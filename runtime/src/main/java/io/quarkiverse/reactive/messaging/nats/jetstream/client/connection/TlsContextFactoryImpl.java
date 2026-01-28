package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;
import io.quarkus.tls.TlsConfiguration;
import io.quarkus.tls.TlsConfigurationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

@ApplicationScoped
@RequiredArgsConstructor
@JBossLog
public class TlsContextFactoryImpl implements TlsContextFactory {
    private final ConnectorConfiguration configuration;
    private final TlsConfigurationRegistry registry;

    @Produces
    @Override
    public TlsContext create() {
        try {
            if (configuration.connection().sslEnabled().orElse(false)) {
                final var tlsConfiguration = configuration.connection().tlsConfigurationName()
                        .flatMap(registry::get)
                        .orElseGet(this::getDefaultTlsConfiguration);
                return new TlsContextImpl(Optional.of(tlsConfiguration.createSSLContext()));
            } else {
                return new TlsContextImpl(Optional.empty());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private TlsConfiguration getDefaultTlsConfiguration() {
        return registry.getDefault().orElseThrow(
                () -> new IllegalStateException("No Quarkus TLS configuration found for NATS JetStream connection"));
    }
}
