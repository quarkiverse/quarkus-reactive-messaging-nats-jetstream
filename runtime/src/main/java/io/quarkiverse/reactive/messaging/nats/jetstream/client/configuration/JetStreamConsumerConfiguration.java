package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.io.JetStreamConsumerType;

public interface JetStreamConsumerConfiguration {

    JetStreamConsumerType type();

    String stream();

    String subject();

    Optional<String> durable();

    List<String> filterSubjects();

    Optional<Duration> ackWait();

    Optional<DeliverPolicy> deliverPolicy();

    Optional<Long> startSeq();

    Optional<ZonedDateTime> startTime();

    Optional<String> description();

    Optional<Duration> inactiveThreshold();

    Optional<Integer> maxAckPending();

    Optional<Integer> maxDeliver();

    Optional<ReplayPolicy> replayPolicy();

    Optional<Integer> replicas();

    Optional<Boolean> memoryStorage();

    Optional<String> sampleFrequency();

    Map<String, String> metadata();

    List<Duration> backoff();
}
