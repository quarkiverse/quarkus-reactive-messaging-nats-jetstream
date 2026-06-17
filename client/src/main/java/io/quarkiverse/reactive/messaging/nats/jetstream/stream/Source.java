package io.quarkiverse.reactive.messaging.nats.jetstream.stream;

import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record Source(String name,
                     long startSeq,
                     ZonedDateTime startTime,
                     String filterSubject,
                     External external,
                     List<SubjectTransform> subjectTransforms,
                     ConsumerSource consumerSource) {
}
