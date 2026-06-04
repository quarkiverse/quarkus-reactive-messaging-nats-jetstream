package io.quarkiverse.reactive.nats.jetstream.message;

import java.util.List;
import java.util.Map;

public interface Headers extends Map<String, List<String>> {

    static Headers of() {
        return new DefaultHeaders();
    }

    static Headers of(Map<String, List<String>> headers) {
        return new DefaultHeaders(headers);
    }
}
