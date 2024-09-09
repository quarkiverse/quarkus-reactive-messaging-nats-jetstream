package io.quarkiverse.reactive.messaging.nats.jetstream.client.administration;

import java.util.List;

import lombok.Builder;

@Builder
public record LostStreamDataState(List<Long> messages, Long bytes) {

    static LostStreamDataState of(io.nats.client.api.LostStreamData lostStreamData) {
        return LostStreamDataState.builder().messages(lostStreamData.getMessages()).bytes(lostStreamData.getBytes()).build();
    }
}
