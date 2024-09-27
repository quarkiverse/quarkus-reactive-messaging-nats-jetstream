package io.quarkiverse.reactive.messaging.nats.jetstream.client.administration;

import io.nats.client.api.Error;
import io.nats.client.api.External;
import io.nats.client.api.SubjectTransform;
import lombok.Builder;

import java.time.Duration;
import java.util.List;

@Builder
public record Mirror(String name,
                     long lag,
                     Duration active,
                     External external,
                     List<SubjectTransform> subjectTransforms,
                     Error error) {
}
