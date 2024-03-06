package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.time.Duration;

public class ExponentialBackoff {
    private boolean enabled;
    private Duration maxDuration;

    public ExponentialBackoff(boolean enabled, Duration maxDuration) {
        this.enabled = enabled;
        this.maxDuration = maxDuration;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Duration getMaxDuration() {
        return maxDuration;
    }

    public Duration getDuration(long deliveryCount) {
        long backoffSeconds = Math.round(Math.pow(2D, deliveryCount));
        return backoffSeconds < maxDuration.getSeconds() ? Duration.ofSeconds(backoffSeconds) : maxDuration;
    }
}
