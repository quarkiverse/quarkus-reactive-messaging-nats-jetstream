package io.quarkiverse.reactive.messaging.nats.jetstream.reply;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.config.Config;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkus.runtime.ShutdownEvent;
import io.smallrye.reactive.messaging.EmitterConfiguration;
import io.smallrye.reactive.messaging.EmitterFactory;
import io.smallrye.reactive.messaging.annotations.EmitterFactoryFor;

/**
 * Creates {@link JetStreamRequestReply} emitters for channels whose injection type is
 * {@code JetStreamRequestReply}. Handler beans are resolved lazily per channel so that different channels can use
 * different correlation-id and failure handlers. Emitters are closed on application shutdown: their reply subscription is
 * cancelled, the ephemeral consumer deleted and outstanding requests failed.
 */
@ApplicationScoped
@EmitterFactoryFor(JetStreamRequestReply.class)
public class JetStreamRequestReplyFactory implements EmitterFactory<JetStreamRequestReplyImpl<Object, Object>> {

    private final Client client;
    private final Config config;
    private final Instance<CorrelationIdHandler> correlationIdHandlers;
    private final Instance<ReplyFailureHandler> failureHandlers;

    private final Set<JetStreamRequestReplyImpl<?, ?>> emitters = ConcurrentHashMap.newKeySet();

    public JetStreamRequestReplyFactory(final Client client,
            final Config config,
            @Any Instance<CorrelationIdHandler> correlationIdHandlers,
            @Any Instance<ReplyFailureHandler> failureHandlers) {
        this.client = client;
        this.config = config;
        this.correlationIdHandlers = correlationIdHandlers;
        this.failureHandlers = failureHandlers;
    }

    @Override
    public JetStreamRequestReplyImpl<Object, Object> createEmitter(EmitterConfiguration configuration, long defaultBufferSize) {
        final var emitter = new JetStreamRequestReplyImpl<>(configuration, client, config, correlationIdHandlers,
                failureHandlers);
        emitters.add(emitter);
        return emitter;
    }

    void onShutdown(@Observes ShutdownEvent event) {
        emitters.forEach(JetStreamRequestReplyImpl::close);
    }

}
