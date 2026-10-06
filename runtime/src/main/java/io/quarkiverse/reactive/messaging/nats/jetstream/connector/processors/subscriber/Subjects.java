package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.subscriber;

import org.jspecify.annotations.NonNull;

/**
 * Matches NATS subjects against subject patterns, where {@code *} matches exactly one token and {@code >} matches
 * one or more trailing tokens.
 */
final class Subjects {

    private Subjects() {
    }

    static boolean matches(@NonNull String pattern, @NonNull String subject) {
        final var patternTokens = pattern.split("\\.", -1);
        final var subjectTokens = subject.split("\\.", -1);
        for (int i = 0; i < patternTokens.length; i++) {
            if (patternTokens[i].equals(">")) {
                return i == patternTokens.length - 1 && subjectTokens.length > i;
            }
            if (i >= subjectTokens.length) {
                return false;
            }
            if (!patternTokens[i].equals("*") && !patternTokens[i].equals(subjectTokens[i])) {
                return false;
            }
        }
        return patternTokens.length == subjectTokens.length;
    }
}
