package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.tls.TlsConfigurationRegistry;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class TlsContextFactoryImpl implements TlsContextFactory {
    private final ClientConfiguration connectorConfiguration;
    private final TlsConfigurationRegistry tlsConfigurationRegistry;
    private final Map<String, TlsContext> cached = new HashMap<>();

    @Override
    public TlsContext create(String name) {
        return Optional.ofNullable(cached.get(name))
                .orElseThrow(() -> new IllegalArgumentException("No TLS context found for name: " + name));
    }

    @PostConstruct
    void init() {
        addTlsContext(connectorConfiguration);
        connectorConfiguration.namedDatasource().values().forEach(this::addTlsContext);
    }

    private void addTlsContext(DataSourceConfiguration datasource) {
        datasource.connection().tlsConfigurationName().ifPresent(tlsConfigurationName -> {
            try {
                final var tlsConfiguration = tlsConfigurationRegistry.get(tlsConfigurationName)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No TLS configuration found for name: " + tlsConfigurationName));
                cached.put(tlsConfigurationName, new TlsContextRecord(tlsConfiguration.createSSLContext()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
