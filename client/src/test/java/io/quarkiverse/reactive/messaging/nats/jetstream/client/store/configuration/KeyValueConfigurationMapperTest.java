package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class KeyValueConfigurationMapperTest {

    @Test
    void limitMarkerTtlEnablesPerMessageTtl() {
        final var configuration = KeyValueConfiguration.of(configuration(Optional.of(Duration.ofSeconds(5))));

        assertThat(configuration.getLimitMarkerTtl()).isEqualTo(Duration.ofSeconds(5));
        assertThat(configuration.getBackingConfig().getSubjectDeleteMarkerTtl()).isEqualTo(Duration.ofSeconds(5));
        assertThat(configuration.getBackingConfig().isAllowMessageTtl()).isTrue();
    }

    @Test
    void noLimitMarkerTtlLeavesPerMessageTtlDisabled() {
        final var configuration = KeyValueConfiguration.of(configuration(Optional.empty()));

        assertThat(configuration.getLimitMarkerTtl()).isNull();
        assertThat(configuration.getBackingConfig().isAllowMessageTtl()).isFalse();
    }

    @Test
    void limitMarkerTtlIsMappedBack() {
        final var configuration = KeyValueConfiguration
                .of(KeyValueConfiguration.of(configuration(Optional.of(Duration.ofSeconds(5)))));

        assertThat(configuration.limitMarkerTtl()).contains(Duration.ofSeconds(5));
    }

    private static KeyValueConfiguration configuration(Optional<Duration> limitMarkerTtl) {
        return KeyValueConfigurationImpl.builder()
                .bucketName("test")
                .description(Optional.empty())
                .storageType(StorageType.Memory)
                .maxBucketSize(Optional.empty())
                .maxHistoryPerKey(1)
                .ttl(Optional.empty())
                .replicas(1)
                .compression(false)
                .placement(Optional.empty())
                .metadata(Map.of())
                .maximumValueSize(Optional.empty())
                .mirror(Optional.empty())
                .limitMarkerTtl(limitMarkerTtl)
                .sources(List.of())
                .republish(Optional.empty())
                .build();
    }
}
