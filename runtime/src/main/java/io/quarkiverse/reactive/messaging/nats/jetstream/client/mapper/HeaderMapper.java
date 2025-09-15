package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.nats.client.impl.Headers;

public class HeaderMapper {

    public static Map<String, List<String>> toMessageHeaders(Headers headers) {
        final var result = new HashMap<String, List<String>>();
        if (headers != null) {
            headers.entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        }
        return result;
    }

}
