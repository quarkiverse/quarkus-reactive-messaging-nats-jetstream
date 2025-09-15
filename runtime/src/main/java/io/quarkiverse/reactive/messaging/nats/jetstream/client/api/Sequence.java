package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.time.ZonedDateTime;

public record Sequence(long consumerSequence,
        long streamSequence,
        ZonedDateTime lastActive) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long consumerSequence;
        private long streamSequence;
        private ZonedDateTime lastActive;

        public Builder consumerSequence(long consumerSequence) {
            this.consumerSequence = consumerSequence;
            return this;
        }

        public Builder streamSequence(long streamSequence) {
            this.streamSequence = streamSequence;
            return this;
        }

        public Builder lastActive(ZonedDateTime lastActive) {
            this.lastActive = lastActive;
            return this;
        }

        public Sequence build() {
            return new Sequence(consumerSequence, streamSequence, lastActive);
        }
    }
}
