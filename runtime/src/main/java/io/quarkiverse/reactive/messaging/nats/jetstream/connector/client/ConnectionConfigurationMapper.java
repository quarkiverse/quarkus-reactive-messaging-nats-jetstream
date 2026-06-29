package io.quarkiverse.reactive.messaging.nats.jetstream.connector.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectorConfiguration;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface ConnectionConfigurationMapper {

    io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionConfiguration map(ConnectionConfiguration configuration);
}
