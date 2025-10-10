package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import io.nats.client.impl.Headers;

@ApplicationScoped
public class HeaderMapperImpl implements HeaderMapper {

    @Override
    public Map<String, List<String>> map(Headers headers) {
        final var result = new HashMap<String, List<String>>();
        if (headers != null) {
            headers.entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}
