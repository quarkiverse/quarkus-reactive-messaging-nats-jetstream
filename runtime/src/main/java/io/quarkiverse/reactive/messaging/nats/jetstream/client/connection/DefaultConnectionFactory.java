package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.DefaultErrorListener;
import io.quarkus.tls.TlsConfiguration;
import io.quarkus.tls.TlsConfigurationRegistry;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import static io.nats.client.Options.DEFAULT_RECONNECT_WAIT;

@ApplicationScoped
@RequiredArgsConstructor
public class DefaultConnectionFactory implements ConnectionFactory {
    private final ConnectionConfiguration configuration;
    private final TlsConfigurationRegistry tlsConfigurationRegistry;

    @Override
    public @NonNull Uni<Connection> create() {
        return Uni.createFrom().item(Unchecked.supplier(this::createOptions))
                .onItem().<Connection>transformToUni(options -> Uni.createFrom().item(Unchecked.supplier(() -> new DefaultConnection(Nats.connect(options)))))
                .onFailure().transform(ConnectionException::new);
    }

    private @NonNull Options createOptions() throws Exception {
        final var optionsBuilder = new Options.Builder();
        final var servers = configuration.servers();
        optionsBuilder.servers(servers.toArray(new String[0]));
        optionsBuilder.maxReconnects(configuration.connectionAttempts());
        optionsBuilder.connectionTimeout(configuration.connectionBackoff().orElse(DEFAULT_RECONNECT_WAIT));

        optionsBuilder.connectionListener(new DefaultNativeConnectionListener());
        optionsBuilder.errorListener(getErrorListener(configuration));
        configuration.username()
                .ifPresent(username -> optionsBuilder.userInfo(username, configuration.password().orElse("")));
        configuration.token().map(String::toCharArray).ifPresent(optionsBuilder::token);
        configuration.credentialPath().ifPresent(optionsBuilder::credentialPath);
        configuration.bufferSize().ifPresent(optionsBuilder::bufferSize);
        configuration.connectionTimeout().ifPresent(optionsBuilder::connectionTimeout);
        if (configuration.sslEnabled().orElse(false)) {
            optionsBuilder.opentls();
            final var tlsConfiguration = configuration.tlsConfigurationName()
                    .flatMap(tlsConfigurationRegistry::get)
                    .orElseGet(() -> getDefaultTlsConfiguration(tlsConfigurationRegistry));
            optionsBuilder.sslContext(tlsConfiguration.createSSLContext());
        }
        configuration.tlsAlgorithm().ifPresent(optionsBuilder::tlsAlgorithm);
        return optionsBuilder.build();
    }

    private @NonNull ErrorListener getErrorListener(@NonNull ConnectionConfiguration configuration) {
        return configuration.errorListener()
                .orElseGet(DefaultErrorListener::new);
    }

    private @NonNull TlsConfiguration getDefaultTlsConfiguration(@NonNull TlsConfigurationRegistry tlsConfigurationRegistry) {
        return tlsConfigurationRegistry.getDefault().orElseThrow(
                () -> new IllegalStateException("No Quarkus TLS configuration found for NATS JetStream connection"));
    }
}
