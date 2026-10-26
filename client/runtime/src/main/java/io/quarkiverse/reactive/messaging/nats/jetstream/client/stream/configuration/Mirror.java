package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * Mirror Information. Maintains a 1:1 mirror of another stream with name matching this property.
 * When a mirror is configured subjects and sources must be empty.
 */
public interface Mirror {

    /**
     * Get the name of the source. Same as getName()
     *
     * @return get the source name
     */
    @NonNull
    String sourceName();

    /**
     * Get the name of the source. Same as getSourceName()
     *
     * @return the source name
     */
    @NonNull
    String name();

    /**
     * Get the configured start sequence
     *
     * @return the start sequence
     */
    long startSequence();

    /**
     * Get the configured start time
     *
     * @return the start time
     */
    @NonNull
    Optional<ZonedDateTime> startTime();

    /**
     * Get the configured filter subject
     *
     * @return the filter subject
     */
    @NonNull
    Optional<String> filterSubject();

    /**
     * Get the External reference
     *
     * @return the External
     */
    @NonNull
    Optional<External> external();

    /**
     * Get the subject transforms
     *
     * @return the list of subject transforms
     */
    @NonNull
    List<SubjectTransform> subjectTransforms();

    /**
     * Get the consumer source for durable sourcing
     *
     * @return the consumer source
     */
    @NonNull
    Optional<ConsumerSource> consumerSource();

}
