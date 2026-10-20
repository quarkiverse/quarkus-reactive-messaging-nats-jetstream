package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.reply;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.CorrelationIdHandler;
import io.smallrye.common.annotation.Identifier;

/**
 * Test correlation id handler: always generates the same id so tests can publish matching replies out-of-band.
 */
@ApplicationScoped
@Identifier("fixed-id")
public class FixedCorrelationIdHandler implements CorrelationIdHandler {

    public static final String ID = "fixed-correlation";

    @Override
    public String generate() {
        return ID;
    }

    @Override
    public Optional<String> parse(String value) {
        return Optional.of(value);
    }

}
