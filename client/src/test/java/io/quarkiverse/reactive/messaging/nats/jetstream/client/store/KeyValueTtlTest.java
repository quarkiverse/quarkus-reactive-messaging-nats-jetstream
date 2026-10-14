package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class KeyValueTtlTest {

    @Test
    void wholeSecondsAreKept() {
        assertThat(KeyValue.toMessageTtl(Duration.ofSeconds(30)).getTtlString()).isEqualTo("30s");
    }

    @Test
    void fractionalSecondsAreRoundedUp() {
        assertThat(KeyValue.toMessageTtl(Duration.ofMillis(1500)).getTtlString()).isEqualTo("2s");
        assertThat(KeyValue.toMessageTtl(Duration.ofMillis(1)).getTtlString()).isEqualTo("1s");
    }

    @Test
    void nonPositiveDurationsAreRejected() {
        assertThatThrownBy(() -> KeyValue.toMessageTtl(Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> KeyValue.toMessageTtl(Duration.ofSeconds(-1))).isInstanceOf(IllegalArgumentException.class);
    }
}
