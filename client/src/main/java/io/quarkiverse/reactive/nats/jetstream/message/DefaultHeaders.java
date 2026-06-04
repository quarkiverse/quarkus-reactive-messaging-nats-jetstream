package io.quarkiverse.reactive.nats.jetstream.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DefaultHeaders extends HashMap<String, List<String>> implements Headers {

    public DefaultHeaders(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public DefaultHeaders(int initialCapacity) {
        super(initialCapacity);
    }

    public DefaultHeaders() {
    }

    public DefaultHeaders(Map<? extends String, ? extends List<String>> m) {
        super(m);
    }
}
