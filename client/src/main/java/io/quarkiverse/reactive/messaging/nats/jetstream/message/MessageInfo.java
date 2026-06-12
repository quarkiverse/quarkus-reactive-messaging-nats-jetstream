package io.quarkiverse.reactive.messaging.nats.jetstream.message;

import org.jspecify.annotations.NonNull;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface MessageInfo {

    static MessageInfo of(io.nats.client.api.MessageInfo messageInfo) {
        return new MessageInfoRecord(messageInfo);
    }

    @NonNull Optional<String> subject();

    long sequence();

    @NonNull Optional<byte[]> payload();

    @NonNull Optional<ZonedDateTime> timestamp();

    @NonNull Headers headers();

    /**
     * Get the name of the stream. Not always set.
     * @return the stream name or null if the name is not known.
     */
    @NonNull Optional<String> stream();

    /**
     * Get the sequence number of the last message in the stream. Not always set.
     * @return the last sequence or -1 if the value is not known.
     */
    long lastSequence();

    /**
     * Amount of pending messages that can be requested with a subsequent batch request.
     * @return number of pending messages
     */
    long numberOfPendingMessages();

    Optional<Status> status();
}
