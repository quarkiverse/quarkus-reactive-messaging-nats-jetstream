package io.quarkiverse.reactive.messaging.nats.jetstream.reply;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;

import io.smallrye.common.annotation.Identifier;

/**
 * The default {@link CorrelationIdHandler}: generates random version-4 UUIDs and validates incoming values as UUIDs.
 */
@ApplicationScoped
@Identifier("uuid")
public class UuidCorrelationIdHandler implements CorrelationIdHandler {

    public static final String ID = "uuid";

    private static final Pattern UUID_PATTERN = Pattern
            .compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Optional<String> parse(String value) {
        if (value == null || !UUID_PATTERN.matcher(value).matches()) {
            return Optional.empty();
        }
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return Optional.of(value.toLowerCase());
    }
}
