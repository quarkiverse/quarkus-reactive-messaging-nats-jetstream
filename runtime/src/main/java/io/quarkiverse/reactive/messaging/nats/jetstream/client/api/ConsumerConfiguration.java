package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import io.nats.client.api.AckPolicy;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import lombok.Builder;

@Builder
public record ConsumerConfiguration(DeliverPolicy deliverPolicy,
        AckPolicy ackPolicy,
        ReplayPolicy replayPolicy,
        String description,
        String durable,
        String name,
        String deliverSubject,
        String deliverGroup,
        String sampleFrequency,
        ZonedDateTime startTime,
        Duration ackWait,
        Duration idleHeartbeat,
        Duration maxExpires,
        Duration inactiveThreshold,
        Long startSequence, // server side this is unsigned
        Long maxDeliver,
        Long rateLimit, // server side this is unsigned
        Long maxAckPending,
        Long maxPullWaiting,
        Long maxBatch,
        Long maxBytes,
        Integer numReplicas,
        ZonedDateTime pauseUntil,
        Boolean flowControl,
        Boolean headersOnly,
        Boolean memStorage,
        List<Duration> backoff,
        Map<String, String> metadata,
        List<String> filterSubjects) {

}
