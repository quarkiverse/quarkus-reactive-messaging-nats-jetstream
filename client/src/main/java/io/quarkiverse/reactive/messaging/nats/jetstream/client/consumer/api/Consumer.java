package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;

/**
 * This interface represents a consumer in a streaming or messaging system. A consumer retrieves
 * messages from a specific stream and may operate based on several configurations.
 */
public interface Consumer {

    /**
     * The Stream the consumer belongs to
     */
    @NonNull
    String stream();

    /**
     * A unique name for the consumer, either machine generated or the durable name
     */
    @NonNull
    String name();

    /**
     * The consumer configuration representing this consumer.
     */
    @NonNull
    ConsumerConfiguration configuration();

    /**
     * The creation time of the consumer
     */
    @NonNull
    ZonedDateTime created();

    /**
     * The last message delivered from this Consumer
     */
    @NonNull
    Sequence delivered();

    /**
     * The highest contiguous acknowledged message
     */
    @NonNull
    Sequence ackFloor();

    /**
     * The number of messages left unconsumed in this Consumer
     */
    long pending();

    /**
     * The number of pull consumers waiting for messages
     */
    long waiting();

    /**
     * The number of messages pending acknowledgement
     */
    long acknowledgePending();

    /**
     * The number of redeliveries that have been performed
     */
    long redelivered();

    /**
     * Indicates if the consumer is currently in a paused state
     */
    boolean paused();

    /**
     * When paused the time remaining until unpause
     */
    @NonNull
    Optional<Duration> pauseRemaining();

    /**
     * Indicates if any client is connected and receiving messages from a push consumer
     */
    boolean pushBound();

    /**
     * The server time the info was gathered
     */
    @NonNull
    Optional<ZonedDateTime> timestamp();

    static Consumer of(io.nats.client.api.ConsumerInfo consumerInfo) {
        final var mapper = Mappers.getMapper(ConsumerMapper.class);
        return mapper.map(consumerInfo);
    }
}
