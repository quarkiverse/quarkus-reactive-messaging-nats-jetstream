package io.quarkiverse.reactive.messaging.nats.jetstream.test.resources;

import java.util.List;

public record StreamInfo(String name, List<String> subjects) {
}
