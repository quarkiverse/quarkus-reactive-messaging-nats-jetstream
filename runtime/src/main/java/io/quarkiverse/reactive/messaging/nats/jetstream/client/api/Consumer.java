package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.time.Duration;
import java.time.ZonedDateTime;

public record Consumer(String stream,
        String name,
        ConsumerConfiguration configuration,
        ZonedDateTime created,
        Sequence delivered,
        Sequence ackFloor,
        Long pending,
        Long waiting,
        Long acknowledgePending,
        Long redelivered,
        Boolean paused,
        Duration pauseRemaining,
        Cluster cluster,
        Boolean pushBound,
        ZonedDateTime timestamp) {

    public static class Builder {
        private String stream;
        private String name;
        private ConsumerConfiguration configuration;
        private ZonedDateTime created;
        private Sequence delivered;
        private Sequence ackFloor;
        private Long pending;
        private Long waiting;
        private Long acknowledgePending;
        private Long redelivered;
        private Boolean paused;
        private Duration pauseRemaining;
        private Cluster cluster;
        private Boolean pushBound;
        private ZonedDateTime timestamp;

        public Builder stream(String stream) {
            this.stream = stream;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder configuration(ConsumerConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder created(ZonedDateTime created) {
            this.created = created;
            return this;
        }

        public Builder delivered(Sequence delivered) {
            this.delivered = delivered;
            return this;
        }

        public Builder ackFloor(Sequence ackFloor) {
            this.ackFloor = ackFloor;
            return this;
        }

        public Builder pending(Long pending) {
            this.pending = pending;
            return this;
        }

        public Builder waiting(Long waiting) {
            this.waiting = waiting;
            return this;
        }

        public Builder acknowledgePending(Long acknowledgePending) {
            this.acknowledgePending = acknowledgePending;
            return this;
        }

        public Builder redelivered(Long redelivered) {
            this.redelivered = redelivered;
            return this;
        }

        public Builder paused(Boolean paused) {
            this.paused = paused;
            return this;
        }

        public Builder pauseRemaining(Duration pauseRemaining) {
            this.pauseRemaining = pauseRemaining;
            return this;
        }

        public Builder cluster(Cluster cluster) {
            this.cluster = cluster;
            return this;
        }

        public Builder pushBound(Boolean pushBound) {
            this.pushBound = pushBound;
            return this;
        }

        public Builder timestamp(ZonedDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Consumer build() {
            return new Consumer(stream, name, configuration, created, delivered, ackFloor, pending, waiting, acknowledgePending, redelivered, paused, pauseRemaining, cluster, pushBound, timestamp);
        }
    }
}
