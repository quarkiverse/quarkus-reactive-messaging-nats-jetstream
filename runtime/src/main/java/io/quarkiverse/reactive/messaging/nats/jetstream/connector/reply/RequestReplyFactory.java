package io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientRegistry;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ChannelConfigurationFactory;
import io.smallrye.reactive.messaging.EmitterConfiguration;
import io.smallrye.reactive.messaging.EmitterFactory;
import io.smallrye.reactive.messaging.annotations.EmitterFactoryFor;

/**
 * Creates {@link RequestReply} emitters for channels whose injection type is
 * {@code JetStreamRequestReply}. Handler beans are resolved lazily per channel so that different channels can use
 * different correlation-id and failure handlers. Emitters are closed on application shutdown: their reply subscription is
 * cancelled, the ephemeral consumer deleted and outstanding requests failed.
 */
@ApplicationScoped
@EmitterFactoryFor(RequestReply.class)
public class RequestReplyFactory implements EmitterFactory<RequestReplyImpl<Object, Object>> {
    private final ClientRegistry clientRegistry;
    private final Config config;
    private final ChannelConfigurationFactory channelConfigurationFactory;

    private final Set<RequestReplyImpl<?, ?>> emitters = ConcurrentHashMap.newKeySet();

    public RequestReplyFactory(final ClientRegistry clientRegistry,
            final Config config,
            final ChannelConfigurationFactory channelConfigurationFactory) {
        this.clientRegistry = clientRegistry;
        this.config = config;
        this.channelConfigurationFactory = channelConfigurationFactory;
    }

    @Override
    public RequestReplyImpl<Object, Object> createEmitter(EmitterConfiguration configuration, long defaultBufferSize) {
        final var emitter = new RequestReplyImpl<>(
                configuration,
                clientRegistry,
                channelConfigurationFactory.create(configuration.name(), config));
        emitters.add(emitter);
        return emitter;
    }

    @PreDestroy
    public void close() {
        emitters.forEach(RequestReplyImpl::reset);
    }
}
