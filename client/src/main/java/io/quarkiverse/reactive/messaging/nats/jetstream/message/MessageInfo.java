package io.quarkiverse.reactive.messaging.nats.jetstream.message;

import java.time.ZonedDateTime;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import lombok.Builder;

@Builder
public record MessageInfo(@Nullable String subject,
        long sequence,
        byte @Nullable [] payload,
        @Nullable ZonedDateTime timestamp,
        @NonNull Headers headers,
        @Nullable String stream,
        long lastSequence,
        long numberOfPendingMessages,
        @Nullable Status status) {

    public static MessageInfo of(io.nats.client.api.MessageInfo messageInfo) {
        return MessageInfo.builder()
                .subject(messageInfo.getSubject())
                .sequence(messageInfo.getSeq())
                .payload(messageInfo.getData())
                .timestamp(messageInfo.getTime())
                .headers(messageInfo.getHeaders() != null ? Headers.of(messageInfo.getHeaders()) : Headers.of())
                .stream(messageInfo.getStream())
                .lastSequence(messageInfo.getLastSeq())
                .numberOfPendingMessages(messageInfo.getNumPending())
                .status(messageInfo.getStatus() != null ? new Status() {
                    @Override
                    public String message() {
                        return messageInfo.getStatus().getMessage();
                    }

                    @Override
                    public int code() {
                        return messageInfo.getStatus().getCode();
                    }

                    @Override
                    public boolean isError() {
                        return messageInfo.isErrorStatus();
                    }
                } : null)
                .build();
    }
}
