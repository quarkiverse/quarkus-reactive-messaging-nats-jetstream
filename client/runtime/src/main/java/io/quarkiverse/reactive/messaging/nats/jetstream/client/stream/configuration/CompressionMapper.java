package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.mapstruct.Mapper;

@Mapper
interface CompressionMapper {

    Compression map(io.nats.client.api.CompressionOption source);

    io.nats.client.api.CompressionOption map(Compression source);
}
