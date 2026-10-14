package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class KeyValueTtlTest {

    @Test
    void wholeSecondsAreKept() {
        assertThat(KeyValueImpl.messageTtl(Duration.ofSeconds(30)).getTtlString()).isEqualTo("30s");
    }

    @Test
    void fractionalSecondsAreRoundedUp() {
        assertThat(KeyValueImpl.messageTtl(Duration.ofMillis(1500)).getTtlString()).isEqualTo("2s");
        assertThat(KeyValueImpl.messageTtl(Duration.ofMillis(1)).getTtlString()).isEqualTo("1s");
    }

    @Test
    void nonPositiveDurationsAreRejected() {
        assertThatThrownBy(() -> KeyValueImpl.messageTtl(Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> KeyValueImpl.messageTtl(Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
