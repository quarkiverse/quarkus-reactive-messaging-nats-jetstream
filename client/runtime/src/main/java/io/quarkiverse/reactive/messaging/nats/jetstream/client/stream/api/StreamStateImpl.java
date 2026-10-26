package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record StreamStateImpl(long messageCount,
        long byteCount,
        long firstSequence,
        @NonNull Optional<ZonedDateTime> firstTime,
        long lastSequence,
        @NonNull Optional<ZonedDateTime> lastTime,
        long consumerCount,
        long subjectCount,
        @NonNull List<Subject> subjects,
        long deletedCount,
        @NonNull List<Long> deleted,
        @NonNull Optional<LostStreamData> lostStreamData) implements StreamState {
}
