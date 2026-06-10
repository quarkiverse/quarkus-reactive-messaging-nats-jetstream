package io.quarkiverse.reactive.messaging.nats.jetstream.message;

import java.util.HashMap;
import java.util.List;

class DefaultHeaders extends HashMap<String, List<String>> implements Headers {

    DefaultHeaders() {
        super();
    }

}
