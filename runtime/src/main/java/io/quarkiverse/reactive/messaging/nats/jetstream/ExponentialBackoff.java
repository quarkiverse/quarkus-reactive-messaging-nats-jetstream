package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.time.Duration;

public record ExponentialBackoff(boolean enabled, Duration maxDuration) {

    public Duration getDuration(long deliveryCount) {
        long backoffSeconds = Math.round(Math.pow(2D, deliveryCount));
        return backoffSeconds < maxDuration.getSeconds() ? Duration.ofSeconds(backoffSeconds) : maxDuration;
    }

}
