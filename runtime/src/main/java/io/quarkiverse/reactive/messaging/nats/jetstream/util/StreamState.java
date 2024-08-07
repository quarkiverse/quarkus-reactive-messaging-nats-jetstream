package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder
public record StreamState(long messageCount,
        long byteCount,
        long firstSequence,
        long lastSequence,
        long consumerCount,
        long subjectCount,
        long deletedCount,
        ZonedDateTime firstTime,
        ZonedDateTime lastTime,
        List<SubjectState> subjects,
        List<Long> deletedStreamSequences,
        LostStreamDataState lostStreamData) {

    static StreamState of(io.nats.client.api.StreamState state) {
        return StreamState.builder()
                .messageCount(state.getMsgCount())
                .byteCount(state.getByteCount())
                .firstSequence(state.getFirstSequence())
                .lastSequence(state.getLastSequence())
                .consumerCount(state.getConsumerCount())
                .subjectCount(state.getSubjectCount())
                .deletedCount(state.getDeletedCount())
                .firstTime(state.getFirstTime())
                .lastTime(state.getLastTime())
                .subjects(state.getSubjects() != null ? state.getSubjects().stream().map(SubjectState::of).toList() : List.of())
                .deletedStreamSequences(state.getDeleted() != null ? state.getDeleted() : List.of())
                .lostStreamData(state.getLostStreamData() != null ? LostStreamDataState.of(state.getLostStreamData()) : null)
                .build();
    }
}
