package io.quarkiverse.reactive.messaging.nats.jetstream.test.misc;

import java.util.List;

public record StreamInfo(String name, List<String> subjects) {
}
