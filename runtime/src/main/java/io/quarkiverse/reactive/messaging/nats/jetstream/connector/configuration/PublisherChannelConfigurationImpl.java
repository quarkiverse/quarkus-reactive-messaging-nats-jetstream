package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.CorrelationIdHandler;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.ReplyFailureHandler;
import lombok.Builder;

@Builder
record PublisherChannelConfigurationImpl(
        @NonNull String name,
        @NonNull String stream,
        @NonNull Optional<Duration> retryBackoff,
        @NonNull String datasource,
        @NonNull String subject,
        @NonNull Optional<String> replySubject,
        @NonNull Optional<Duration> replyTimeout,
        @NonNull Optional<Duration> replyInactiveThreshold,
        @NonNull CorrelationIdHandler replyCorrelationIdHandler,
        @NonNull Optional<ReplyFailureHandler> replyFailureHandler) implements PublisherChannelConfiguration {
}
