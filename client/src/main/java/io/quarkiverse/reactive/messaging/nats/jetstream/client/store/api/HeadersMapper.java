package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mapstruct.Mapper;

import io.nats.client.impl.Headers;

@Mapper
public interface HeadersMapper {

    default Map<String, List<String>> map(Headers value) {
        final var result = new HashMap<String, List<String>>();
        value.forEach(result::put);
        return result;
    }

    default Headers map(Map<String, List<String>> value) {
        final var result = new Headers();
        value.forEach(result::add);
        return result;
    }
}
