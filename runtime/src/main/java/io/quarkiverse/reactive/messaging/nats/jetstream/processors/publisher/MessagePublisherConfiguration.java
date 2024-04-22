package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.RequestReplyConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.RequestReplyMessagePublisherConfiguration;

public interface MessagePublisherConfiguration<T> extends JetStreamConsumerConfiguration {

    String channel();

    Optional<Class<T>> payloadType();

    Duration retryBackoff();

    boolean exponentialBackoff();

    Duration exponentialBackoffMaxDuration();

    boolean traceEnabled();

    static <T> MessagePublisherConfiguration<T> of(RequestReplyConfiguration<T> requestReplyConfiguration) {
        return new RequestReplyMessagePublisherConfiguration<>(requestReplyConfiguration);
    }

}
