package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import lombok.Builder;

import java.time.Duration;
import java.util.List;

@Builder
public record MirrorInfo(String name, String filterSubject, long lag, Duration active, External external,
                         List<SubjectTransform> subjectTransforms, Error error) {
}
