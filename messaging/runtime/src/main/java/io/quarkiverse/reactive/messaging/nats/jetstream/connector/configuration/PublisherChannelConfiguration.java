package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.CorrelationIdHandler;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.ReplyFailureHandler;

public interface PublisherChannelConfiguration extends ChannelConfiguration {

    /**
     * Retrieves the subject associated with the publisher channel configuration.
     * This represents the target subject where messages will be published.
     *
     * @return a non-null string representing the subject
     */
    @NonNull
    String subject();

    /**
     * Retrieves the optional reply subject associated with the publisher channel configuration.
     * The reply subject determines where reply messages are directed for a given request-reply interaction.
     *
     * @return an optional string representing the reply subject, which will be non-empty
     *         if a reply subject is configured, or empty if no reply subject is defined
     */
    @NonNull
    Optional<String> replySubject();

    /**
     * Retrieves the optional reply timeout duration for the publisher channel configuration.
     * The reply timeout determines the maximum time to wait for a reply in a request-reply interaction.
     *
     * @return an optional {@code Duration} representing the reply timeout. It will be non-empty if a timeout is configured,
     *         or empty if no timeout is defined.
     */
    @NonNull
    Optional<Duration> replyTimeout();

    /**
     * Retrieves the optional duration that defines the inactivity threshold
     * for receiving replies in a publisher channel configuration. The inactivity
     * threshold represents the duration after which a reply is considered inactive
     * if no response is received.
     *
     * @return an optional {@code Duration} representing the inactivity threshold
     *         for replies. The value will be non-empty if an inactivity threshold
     *         is configured, or empty if no such threshold is defined.
     */
    @NonNull
    Optional<Duration> replyInactiveThreshold();

    /**
     * Retrieves the {@code CorrelationIdHandler} associated with the publisher channel configuration.
     * The {@code CorrelationIdHandler} is responsible for generating and parsing correlation IDs
     * used to match requests with their corresponding replies in a request-reply interaction.
     *
     * @return a non-null {@code CorrelationIdHandler} instance
     */
    @NonNull
    CorrelationIdHandler replyCorrelationIdHandler();

    /**
     * Retrieves the optional {@code ReplyFailureHandler} associated with the publisher channel configuration.
     * The {@code ReplyFailureHandler} is responsible for determining whether an incoming reply payload
     * represents a business failure that should result in a failure propagated to the caller, or a normal response.
     *
     * @return an {@code Optional} containing the {@code ReplyFailureHandler} if configured, or an empty {@code Optional}
     *         if no failure handling strategy is defined.
     */
    @NonNull
    Optional<ReplyFailureHandler> replyFailureHandler();
}
