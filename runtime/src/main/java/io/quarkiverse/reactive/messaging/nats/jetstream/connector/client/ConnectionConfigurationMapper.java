package io.quarkiverse.reactive.messaging.nats.jetstream.connector.client;

import java.util.Optional;

import javax.net.ssl.SSLContext;

import jakarta.enterprise.inject.spi.CDI;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectionConfiguration;

@Mapper(componentModel = "cdi")
public interface ConnectionConfigurationMapper {

    @Mapping(target = "servers", expression = "java(configuration.servers())")
    @Mapping(target = "username", expression = "java(configuration.username())")
    @Mapping(target = "password", expression = "java(configuration.password())")
    @Mapping(target = "token", expression = "java(configuration.token())")
    @Mapping(target = "timeout", expression = "java(configuration.timeout())")
    @Mapping(target = "maximumReconnects", expression = "java(configuration.maximumReconnects())")
    @Mapping(target = "errorListener", expression = "java(configuration.errorListener())")
    @Mapping(target = "bufferSize", expression = "java(configuration.bufferSize())")
    @Mapping(target = "tlsAlgorithm", expression = "java(configuration.tlsAlgorithm())")
    @Mapping(target = "credentialPath", expression = "java(configuration.credentialPath())")
    @Mapping(target = "sslContext", expression = "java(sslContext(configuration))")
    ConnectionConfigurationRecord map(ConnectionConfiguration configuration);

    default Optional<SSLContext> sslContext(final ConnectionConfiguration configuration) {
        final var factory = CDI.current().select(TlsContextFactory.class).get();
        return configuration.tlsConfigurationName()
                .map(factory::create)
                .map(TlsContext::sslContext);
    }

}
