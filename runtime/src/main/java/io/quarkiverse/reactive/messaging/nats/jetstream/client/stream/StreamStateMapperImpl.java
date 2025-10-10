package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamState;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubjectState;

@ApplicationScoped
public class StreamStateMapperImpl implements StreamStateMapper {

    @Override
    public StreamState map(io.nats.client.api.StreamState state) {
        return StreamState.builder()
                .messages(state.getMsgCount())
                .bytes(state.getByteCount())
                .consumers(state.getConsumerCount())
                .subjects(state.getSubjectCount())
                .deleted(state.getDeletedCount())
                .subjectStates(subjectStates(state.getSubjects()))
                .subjectMessages(state.getSubjectMap())
                .deletedStreamSequences(state.getDeleted())
                .build();
    }

    private List<SubjectState> subjectStates(List<io.nats.client.api.Subject> states) {
        if (states != null) {
            return states.stream().map(this::subjectState).toList();
        } else {
            return List.of();
        }
    }

    private SubjectState subjectState(io.nats.client.api.Subject state) {
        return SubjectState.builder()
                .name(state.getName())
                .count(state.getCount())
                .build();
    }
}
