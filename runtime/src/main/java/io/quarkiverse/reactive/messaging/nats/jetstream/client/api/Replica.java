package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.time.Duration;

public record Replica(String name,
        Boolean current,
        Boolean offline,
        Duration active,
        Long lag) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Boolean current;
        private Boolean offline;
        private Duration active;
        private Long lag;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder current(Boolean current) {
            this.current = current;
            return this;
        }

        public Builder offline(Boolean offline) {
            this.offline = offline;
            return this;
        }

        public Builder active(Duration active) {
            this.active = active;
            return this;
        }

        public Builder lag(Long lag) {
            this.lag = lag;
            return this;
        }

        public Replica build() {
            return new Replica(name, current, offline, active, lag);
        }
    }
}
