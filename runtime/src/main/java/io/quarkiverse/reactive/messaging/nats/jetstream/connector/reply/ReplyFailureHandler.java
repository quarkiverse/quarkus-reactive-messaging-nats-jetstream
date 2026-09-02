package io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply;

import java.util.Optional;

/**
 * Strategy for deciding whether an incoming reply payload represents a business failure that should fail the caller's
 * {@link Uni}, or a normal response. Provide an implementation as a CDI bean (for example one reading a status field from
 * a JSON error body) and select it per channel with the {@code reply.failure.handler} connector attribute; when none is
 * configured, every matched reply completes the call normally.
 */
public interface ReplyFailureHandler {

    /**
     * @param payload the deserialized reply payload
     * @return an empty {@link Optional} if this is a successful response, or the failure to propagate to the caller
     */
    Optional<Throwable> failure(Object payload);
}
