package io.quarkiverse.reactive.messaging.nats.test.misc;

import java.util.List;

public record StreamInfo(String name, List<String> subjects) {
}
