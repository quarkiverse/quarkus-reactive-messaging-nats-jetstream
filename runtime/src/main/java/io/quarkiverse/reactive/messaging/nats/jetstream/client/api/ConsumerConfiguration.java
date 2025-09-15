package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import io.nats.client.api.AckPolicy;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;

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
        Integer maxAckPending,
        Integer maxPullWaiting,
        Integer maxBatch,
        Integer maxBytes,
        Integer numReplicas,
        ZonedDateTime pauseUntil,
        Boolean flowControl,
        Boolean headersOnly,
        Boolean memStorage,
        List<Duration> backoff,
        Map<String, String> metadata,
        List<String> filterSubjects) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DeliverPolicy deliverPolicy;
        private AckPolicy ackPolicy;
        private ReplayPolicy replayPolicy;
        private String description;
        private String durable;
        private String name;
        private String deliverSubject;
        private String deliverGroup;
        private String sampleFrequency;
        private ZonedDateTime startTime;
        private Duration ackWait;
        private Duration idleHeartbeat;
        private Duration maxExpires;
        private Duration inactiveThreshold;
        private Long startSequence;
        private Long maxDeliver;
        private Long rateLimit;
        private Integer maxAckPending;
        private Integer maxPullWaiting;
        private Integer maxBatch;
        private Integer maxBytes;
        private Integer numReplicas;
        private ZonedDateTime pauseUntil;
        private Boolean flowControl;
        private Boolean headersOnly;
        private Boolean memStorage;
        private List<Duration> backoff;
        private Map<String, String> metadata;
        private List<String> filterSubjects;

        public Builder deliverPolicy(DeliverPolicy deliverPolicy) {
            this.deliverPolicy = deliverPolicy;
            return this;
        }

        public Builder ackPolicy(AckPolicy ackPolicy) {
            this.ackPolicy = ackPolicy;
            return this;
        }

        public Builder replayPolicy(ReplayPolicy replayPolicy) {
            this.replayPolicy = replayPolicy;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder durable(String durable) {
            this.durable = durable;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder deliverSubject(String deliverSubject) {
            this.deliverSubject = deliverSubject;
            return this;
        }

        public Builder deliverGroup(String deliverGroup) {
            this.deliverGroup = deliverGroup;
            return this;
        }

        public Builder sampleFrequency(String sampleFrequency) {
            this.sampleFrequency = sampleFrequency;
            return this;
        }

        public Builder startTime(ZonedDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder ackWait(Duration ackWait) {
            this.ackWait = ackWait;
            return this;
        }

        public Builder idleHeartbeat(Duration idleHeartbeat) {
            this.idleHeartbeat = idleHeartbeat;
            return this;
        }

        public Builder maxExpires(Duration maxExpires) {
            this.maxExpires = maxExpires;
            return this;
        }

        public Builder inactiveThreshold(Duration inactiveThreshold) {
            this.inactiveThreshold = inactiveThreshold;
            return this;
        }

        public Builder startSequence(Long startSequence) {
            this.startSequence = startSequence;
            return this;
        }

        public Builder maxDeliver(Long maxDeliver) {
            this.maxDeliver = maxDeliver;
            return this;
        }

        public Builder rateLimit(Long rateLimit) {
            this.rateLimit = rateLimit;
            return this;
        }

        public Builder maxAckPending(Integer maxAckPending) {
            this.maxAckPending = maxAckPending;
            return this;
        }

        public Builder maxPullWaiting(Integer maxPullWaiting) {
            this.maxPullWaiting = maxPullWaiting;
            return this;
        }

        public Builder maxBatch(Integer maxBatch) {
            this.maxBatch = maxBatch;
            return this;
        }

        public Builder maxBytes(Integer maxBytes) {
            this.maxBytes = maxBytes;
            return this;
        }

        public Builder numReplicas(Integer numReplicas) {
            this.numReplicas = numReplicas;
            return this;
        }

        public Builder pauseUntil(ZonedDateTime pauseUntil) {
            this.pauseUntil = pauseUntil;
            return this;
        }

        public Builder flowControl(Boolean flowControl) {
            this.flowControl = flowControl;
            return this;
        }

        public Builder headersOnly(Boolean headersOnly) {
            this.headersOnly = headersOnly;
            return this;
        }

        public Builder memStorage(Boolean memStorage) {
            this.memStorage = memStorage;
            return this;
        }

        public Builder backoff(List<Duration> backoff) {
            this.backoff = backoff;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder filterSubjects(List<String> filterSubjects) {
            this.filterSubjects = filterSubjects;
            return this;
        }

        public ConsumerConfiguration build() {
            return new ConsumerConfiguration(deliverPolicy, ackPolicy, replayPolicy, description, durable, name, deliverSubject, deliverGroup, sampleFrequency, startTime, ackWait, idleHeartbeat, maxExpires, inactiveThreshold, startSequence, maxDeliver, rateLimit, maxAckPending, maxPullWaiting, maxBatch, maxBytes, numReplicas, pauseUntil, flowControl, headersOnly, memStorage, backoff, metadata, filterSubjects);
        }
    }
}
