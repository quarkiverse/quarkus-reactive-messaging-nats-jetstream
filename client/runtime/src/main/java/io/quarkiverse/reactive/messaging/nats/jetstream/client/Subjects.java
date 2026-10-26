package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import org.jspecify.annotations.NonNull;

/**
 * Matches NATS subjects against subject patterns, where {@code *} matches exactly one token and {@code >} matches
 * one or more trailing tokens.
 */
public final class Subjects {

    private Subjects() {
    }

    /**
     * @param pattern a subject pattern, possibly containing wildcards
     * @param subject a concrete subject
     * @return true if the subject is matched by the pattern
     */
    public static boolean matches(@NonNull String pattern, @NonNull String subject) {
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

    /**
     * @param pattern a subject pattern, possibly containing wildcards
     * @param other another subject pattern, possibly containing wildcards
     * @return true if every subject matched by {@code other} is also matched by {@code pattern}. For a concrete
     *         {@code other} this is the same as {@link #matches(String, String)}.
     */
    public static boolean covers(@NonNull String pattern, @NonNull String other) {
        final var patternTokens = pattern.split("\\.", -1);
        final var otherTokens = other.split("\\.", -1);
        for (int i = 0; i < patternTokens.length; i++) {
            if (i >= otherTokens.length) {
                return false;
            }
            if (patternTokens[i].equals(">")) {
                return i == patternTokens.length - 1;
            }
            if (otherTokens[i].equals(">")) {
                return false;
            }
            if (patternTokens[i].equals("*")) {
                continue;
            }
            if (!patternTokens[i].equals(otherTokens[i])) {
                return false;
            }
        }
        return patternTokens.length == otherTokens.length;
    }
}
