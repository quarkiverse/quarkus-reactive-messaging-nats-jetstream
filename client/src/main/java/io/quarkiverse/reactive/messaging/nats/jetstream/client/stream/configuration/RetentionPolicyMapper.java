package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.mapstruct.Mapper;

@Mapper
interface RetentionPolicyMapper {

    RetentionPolicy map(io.nats.client.api.RetentionPolicy source);

    io.nats.client.api.RetentionPolicy map(RetentionPolicy republish);

}
