package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.time.Duration;
import java.util.*;

import lombok.Builder;

@Builder
public record NackMetadata(Duration delayWait) {

    public Optional<Duration> delayWaitOptional() {
        return Optional.ofNullable(delayWait);
    }
}
