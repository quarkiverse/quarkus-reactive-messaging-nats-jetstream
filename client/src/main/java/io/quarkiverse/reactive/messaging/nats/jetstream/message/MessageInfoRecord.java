package io.quarkiverse.reactive.messaging.nats.jetstream.message;

import org.jspecify.annotations.NonNull;

import java.time.ZonedDateTime;
import java.util.Optional;

record MessageInfoRecord(io.nats.client.api.MessageInfo delegate) implements MessageInfo {

    @Override
    public @NonNull Optional<String> subject() {
        return Optional.ofNullable(delegate.getSubject());
    }

    @Override
    public long sequence() {
        return delegate.getSeq();
    }

    @Override
    public @NonNull Optional<byte[]> payload() {
        return Optional.ofNullable(delegate.getData());
    }

    @Override
    public @NonNull Optional<ZonedDateTime> timestamp() {
        return Optional.ofNullable(delegate.getTime());
    }

    @Override
    public @NonNull Headers headers() {
        return Optional.ofNullable(delegate.getHeaders()).map(Headers::of).orElseGet(Headers::new);
    }

    @Override
    public @NonNull Optional<String> stream() {
        return Optional.ofNullable(delegate.getStream());
    }

    @Override
    public long lastSequence() {
        return delegate.getLastSeq();
    }

    @Override
    public long numberOfPendingMessages() {
        return delegate.getNumPending();
    }

    @Override
    public Optional<Status> status() {
        return Optional.ofNullable(delegate.getStatus()).map(status -> new Status() {
            @Override
            public String message() {
                return status.getMessage();
            }

            @Override
            public int code() {
                return status.getCode();
            }

            @Override
            public boolean isError() {
                return delegate.isErrorStatus();
            }
        });
    }
}
