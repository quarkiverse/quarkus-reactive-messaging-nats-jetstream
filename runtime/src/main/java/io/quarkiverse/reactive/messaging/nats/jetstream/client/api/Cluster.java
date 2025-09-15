package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.util.List;

public record Cluster(String name, String leader, List<Replica> replicas) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String leader;
        private List<Replica> replicas;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder leader(String leader) {
            this.leader = leader;
            return this;
        }

        public Builder replicas(List<Replica> replicas) {
            this.replicas = replicas;
            return this;
        }

        public Cluster build() {
            return new Cluster(name, leader, replicas);
        }
    }
}
