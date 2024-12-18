package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.nats.client.api.AckPolicy;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;

public interface ConsumerConfiguration<T> {

    String name();

    String stream();

    Optional<String> durable();

    String subject();

    Optional<Duration> ackWait();

    Optional<DeliverPolicy> deliverPolicy();

    Optional<Long> startSequence();

    Optional<ZonedDateTime> startTime();

    Optional<String> description();

    Optional<Duration> inactiveThreshold();

    Optional<Long> maxAckPending();

    Optional<Long> maxDeliver();

    Optional<ReplayPolicy> replayPolicy();

    Optional<Integer> replicas();

    Optional<Boolean> memoryStorage();

    Optional<String> sampleFrequency();

    Map<String, String> metadata();

    List<Duration> backoff();

    Optional<AckPolicy> ackPolicy();

    Optional<ZonedDateTime> pauseUntil();

    Optional<Class<T>> payloadType();
}
