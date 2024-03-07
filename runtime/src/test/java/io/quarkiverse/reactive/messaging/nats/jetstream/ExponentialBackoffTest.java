package io.quarkiverse.reactive.messaging.nats.jetstream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

public class ExponentialBackoffTest {
    @Test
    public void testGetDuration() {
        ExponentialBackoff exponentialBackoff = new ExponentialBackoff(true, Duration.ofMinutes(1));
        Duration duration = exponentialBackoff.getDuration(4);
        assertThat(duration.getSeconds()).isEqualTo(16);

        duration = exponentialBackoff.getDuration(10);
        assertThat(duration.getSeconds()).isEqualTo(60);
    }
}
