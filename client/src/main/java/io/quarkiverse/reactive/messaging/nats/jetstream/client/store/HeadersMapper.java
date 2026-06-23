package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;

@Mapper
interface HeadersMapper {

    default Headers map(io.nats.client.impl.Headers value) {
        final var headers = new Headers();
        value.forEach(headers::put);
        return headers;
    }

    default io.nats.client.impl.Headers map(Headers value) {
        final var headers = new io.nats.client.impl.Headers();
        value.forEach(headers::put);
        return headers;
    }
}
