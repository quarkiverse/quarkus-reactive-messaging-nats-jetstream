package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.time.Duration;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.config.Config;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.JetStreamConnector;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientRegistry;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.CorrelationIdHandler;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.ReplyFailureHandler;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.UuidCorrelationIdHandler;
import io.smallrye.reactive.messaging.providers.helpers.CDIUtils;
import io.smallrye.reactive.messaging.providers.impl.Configs;

@ApplicationScoped
public class ChannelConfigurationFactoryImpl implements ChannelConfigurationFactory {
    private final Instance<CorrelationIdHandler> correlationIdHandlers;
    private final Instance<ReplyFailureHandler> failureHandlers;

    public ChannelConfigurationFactoryImpl(@Any Instance<CorrelationIdHandler> correlationIdHandlers,
            @Any Instance<ReplyFailureHandler> failureHandlers) {
        this.correlationIdHandlers = correlationIdHandlers;
        this.failureHandlers = failureHandlers;
    }

    @Override
    public PublisherChannelConfiguration create(String channel, Config config) {
        final var channelConfig = Configs.outgoing(config, JetStreamConnector.CONNECTOR_NAME, channel);
        final var correlationIdHandlerId = nonBlank(
                channelConfig.getOptionalValue("reply.correlation-id.handler", String.class))
                .orElse(UuidCorrelationIdHandler.ID);
        final var failureHandlerId = nonBlank(channelConfig.getOptionalValue("reply.failure.handler", String.class));
        return PublisherChannelConfigurationImpl.builder()
                .name(channel)
                .stream(channelConfig.getValue("stream", String.class))
                .retryBackoff(channelConfig.getOptionalValue("retry-backoff", Long.class).map(Duration::ofMillis))
                .datasource(
                        channelConfig.getOptionalValue("datasource", String.class).orElse(ClientRegistry.DEFAULT_CLIENT_NAME))
                .subject(channelConfig.getValue("subject", String.class))
                .replySubject(nonBlank(channelConfig.getOptionalValue("reply.subject", String.class)))
                .replyTimeout(channelConfig.getOptionalValue("reply.timeout", Long.class).map(Duration::ofMillis))
                .replyInactiveThreshold(
                        channelConfig.getOptionalValue("reply.inactive-threshold", Long.class).map(Duration::ofMillis))
                .replyCorrelationIdHandler(resolveHandler(correlationIdHandlers, CorrelationIdHandler.class, channel,
                        "reply.correlation-id.handler", correlationIdHandlerId))
                .replyFailureHandler(failureHandlerId.map(
                        id -> resolveHandler(failureHandlers, ReplyFailureHandler.class, channel, "reply.failure.handler", id)))
                .build();
    }

    private <T> T resolveHandler(final Instance<T> candidates, final Class<T> type, final String channelName,
            final String attribute, final String id) {
        try {
            return CDIUtils.getInstanceById(candidates, id).get();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "No " + type.getSimpleName() + " with id '" + id + "' found for channel '" + channelName
                            + "'. Define a CDI bean annotated with @Identifier(\"" + id + "\") or set '" + attribute
                            + "' to an available handler id.",
                    e);
        }
    }

    private Optional<String> nonBlank(final Optional<String> value) {
        return value.map(String::trim).filter(s -> !s.isEmpty());
    }
}
