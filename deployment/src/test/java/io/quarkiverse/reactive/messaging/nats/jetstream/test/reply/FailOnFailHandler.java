package io.quarkiverse.reactive.messaging.nats.jetstream.test.reply;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.reply.ReplyFailureHandler;
import io.smallrye.common.annotation.Identifier;

/**
 * Test failure handler: treats any reply payload containing 'FAIL' as a business failure.
 */
@ApplicationScoped
@Identifier("fail-on-fail")
public class FailOnFailHandler implements ReplyFailureHandler {

    @Override
    public Optional<Throwable> failure(Object payload) {
        if (payload instanceof String s && s.contains("FAIL")) {
            return Optional.of(new IllegalStateException(s));
        }
        return Optional.empty();
    }

}
