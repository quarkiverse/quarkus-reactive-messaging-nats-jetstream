package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

public interface StreamState {

    /**
     * Gets the message count of the stream.
     *
     * @return the message count
     */
    long messageCount();

    /**
     * Gets the byte count of the stream.
     *
     * @return the byte count
     */
    long byteCount();

    /**
     * Gets the first sequence number of the stream. May be 0 if there are no messages.
     *
     * @return a sequence number
     */
    long firstSequence();

    /**
     * Gets the time stamp of the first message in the stream
     *
     * @return the first time
     */
    @NonNull
    Optional<ZonedDateTime> firstTime();

    /**
     * Gets the last sequence of a message in the stream
     *
     * @return a sequence number
     */
    long lastSequence();

    /**
     * Gets the time stamp of the last message in the stream
     *
     * @return the first time
     */
    @NonNull
    Optional<ZonedDateTime> lastTime();

    /**
     * Gets the number of consumers attached to the stream.
     *
     * @return the consumer count
     */
    long consumerCount();

    /**
     * Gets the count of subjects in the stream.
     *
     * @return the subject count
     */
    long subjectCount();

    /**
     * Get a list of the Subject objects. May be empty, for instance
     * if the Stream Info request did not ask for subjects or if there are no subjects.
     *
     * @return the list of subjects
     */
    @NonNull
    List<Subject> subjects();

    /**
     * Gets the count of deleted messages
     *
     * @return the deleted count
     */
    long deletedCount();

    /**
     * Get a list of the Deleted objects. May be empty if the Stream Info request did not ask for subjects
     * or if there are no subjects.
     *
     * @return the list of subjects
     */
    @NonNull
    List<Long> deleted();

    /**
     * Get the lost stream data information if available.
     *
     * @return the LostStreamData
     */
    @NonNull
    Optional<LostStreamData> lostStreamData();

}
