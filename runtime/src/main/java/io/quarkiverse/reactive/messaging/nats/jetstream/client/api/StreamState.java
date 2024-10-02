package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder
public record StreamState(Long messages,
        Long bytes,
        Long firstSequence,
        Long lastSequence,
        Long consumers,
        Long subjects,
        Long deleted,
        ZonedDateTime firstTime,
        ZonedDateTime lastTime,
        List<SubjectState> subjectStates,
        List<Long> deletedStreamSequences,
        LostStreamDataState lostStreamData,
        Map<String, Long> subjectMessages) {

}
