package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import lombok.Builder;

@Builder
public record SubjectState(String name, long count) {

    static SubjectState of(io.nats.client.api.Subject subject) {
        return SubjectState.builder().name(subject.getName()).count(subject.getCount()).build();
    }
}
