package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record StreamState(long msgs,
                          long bytes,
                          long firstSeq,
                          long lastSeq,
                          long consumerCount,
                          long subjectCount,
                          long deletedCount,
                          ZonedDateTime firstTime,
                          ZonedDateTime lastTime,
                          List<Subject> subjects,
                          List<Long> deletedStreamSequences,
                          LostStreamData lostStreamData,
                          Map<String, Long> subjectMap) {
}
