package io.quarkiverse.reactive.messaging.nats.jetstream.reply;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.UuidCorrelationIdHandler;

class UuidCorrelationIdHandlerTest {

    private final UuidCorrelationIdHandler handler = new UuidCorrelationIdHandler();

    @Test
    void generatedIdsParseBack() {
        for (int i = 0; i < 10; i++) {
            final var generated = handler.generate();
            assertThat(UUID.fromString(generated)).isNotNull();
            assertThat(handler.parse(generated)).contains(generated);
        }
    }

    @Test
    void parseIsCaseInsensitive() {
        final var id = UUID.randomUUID().toString();
        assertThat(handler.parse(id.toUpperCase())).contains(id.toLowerCase());
    }

    @Test
    void parseRejectsMalformedValues() {
        assertThat(handler.parse(null)).isEmpty();
        assertThat(handler.parse("")).isEmpty();
        assertThat(handler.parse("not-a-uuid")).isEmpty();
        assertThat(handler.parse("12345678-1234-1234-1234-1234567890a")).isEmpty();
        assertThat(handler.parse("12345678-1234-1234-1234-zz34567890ab")).isEmpty();
    }

    @Test
    void generatedIdsAreUnique() {
        assertThat(handler.generate()).isNotEqualTo(handler.generate());
    }

}
