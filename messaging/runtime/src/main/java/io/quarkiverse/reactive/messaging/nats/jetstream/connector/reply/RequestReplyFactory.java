package io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.config.Config;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.PublisherChannelConfigurationFactory;
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
    private final Instance<Client> clients;
    private final Config config;
    private final PublisherChannelConfigurationFactory channelConfigurationFactory;

    private final Set<RequestReplyImpl<?, ?>> emitters = ConcurrentHashMap.newKeySet();

    public RequestReplyFactory(@Any final Instance<Client> clients,
            final Config config,
            final PublisherChannelConfigurationFactory channelConfigurationFactory) {
        this.clients = clients;
        this.config = config;
        this.channelConfigurationFactory = channelConfigurationFactory;
    }

    @Override
    public RequestReplyImpl<Object, Object> createEmitter(EmitterConfiguration configuration, long defaultBufferSize) {
        final var emitter = new RequestReplyImpl<>(
                configuration,
                clients,
                channelConfigurationFactory.create(configuration.name(), config));
        emitters.add(emitter);
        return emitter;
    }

    @PreDestroy
    public void close() {
        emitters.forEach(RequestReplyImpl::reset);
    }
}
