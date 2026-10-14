package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.nats.client.api.External;
import io.nats.client.api.Mirror;
import io.nats.client.api.StreamConfiguration;

class KeyValueWriteSubjectTest {

    @Test
    void bucketWithoutMirrorWritesToOwnSubject() {
        var configuration = StreamConfiguration.builder().name("KV_cache").subjects("$KV.cache.>").build();

        assertThat(KeyValueWriteSubject.writeSubject("cache", configuration, "key")).isEqualTo("$KV.cache.key");
    }

    @Test
    void mirrorWritesToOriginBucket() {
        var configuration = StreamConfiguration.builder().name("KV_cache-mirror")
                .mirror(Mirror.builder().sourceName("KV_cache").build())
                .build();

        assertThat(KeyValueWriteSubject.writeSubject("cache-mirror", configuration, "key")).isEqualTo("$KV.cache.key");
    }

    @Test
    void mirrorInOtherDomainWritesThroughExternalApiPrefix() {
        var configuration = StreamConfiguration.builder().name("KV_cache-mirror")
                .mirror(Mirror.builder().sourceName("KV_cache")
                        .external(External.builder().api("$JS.hub.API").build())
                        .build())
                .build();

        assertThat(KeyValueWriteSubject.writeSubject("cache-mirror", configuration, "key"))
                .isEqualTo("$JS.hub.API.$KV.cache.key");
    }
}
