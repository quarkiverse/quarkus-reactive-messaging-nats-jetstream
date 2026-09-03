package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.CorrelationIdHandler;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.ReplyFailureHandler;

public interface PublisherChannelConfiguration extends ChannelConfiguration {

    @NonNull
    String subject();

    @NonNull
    Optional<String> replySubject();

    @NonNull
    Optional<Duration> replyTimeout();

    @NonNull
    Optional<Duration> replyInactiveThreshold();

    @NonNull
    CorrelationIdHandler replyCorrelationIdHandler();

    @NonNull
    Optional<ReplyFailureHandler> replyFailureHandler();
}
