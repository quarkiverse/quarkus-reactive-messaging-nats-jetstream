package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record Source(String name,
        long startSeq,
        ZonedDateTime startTime,
        String filterSubject,
        External external,
        List<SubjectTransform> subjectTransforms,
        ConsumerSource consumerSource) {
}
