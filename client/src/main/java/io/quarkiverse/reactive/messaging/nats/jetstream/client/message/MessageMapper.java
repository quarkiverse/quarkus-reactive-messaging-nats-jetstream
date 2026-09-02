package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import org.jspecify.annotations.NonNull;

/**
 * Interface defining methods for mapping and transforming instances of reactive messaging {@code Message}.
 * Implementations of this interface provide mechanisms to process, transform, or enrich messages
 * for use in a reactive messaging context.
 */
public interface MessageMapper {

    /**
     * Creates a new instance of {@code MessageMapper} using the provided {@code Serializer}.
     *
     * @param serializer the {@code Serializer} implementation to be used by the {@code MessageMapper};
     *        must not be null
     * @return a new instance of {@code MessageMapper} configured with the specified serializer
     */
    static MessageMapper of(Serializer serializer) {
        return new MessageMapperImpl(serializer);
    }

    /**
     * Maps the given reactive messaging {@code Message} to a new {@code Message} instance.
     * This method is intended for transforming or enriching the input message while preserving or modifying
     * its payload, metadata, or other contextual information as defined by the implementation.
     *
     * @param message the reactive messaging {@code Message} that serves as the input for the mapping process; must not be null
     * @return a new {@code Message} instance resulting from the mapping process
     */
    Message map(org.eclipse.microprofile.reactive.messaging.@NonNull Message<?> message);

    /**
     * Transforms the given reactive messaging {@code Message} into a new {@code Message} instance.
     * The mapping process typically involves modifying or enriching the original message's payload,
     * metadata, or other contextual information.
     *
     * @param message the input {@code Message} to be transformed; must not be null
     * @return a new {@code Message} instance resulting from the transformation
     */
    <T> org.eclipse.microprofile.reactive.messaging.Message<T> map(@NonNull Message message);

    /**
     * Maps the given reactive messaging {@code Message} to a new {@code Message} instance with a specified payload type.
     * This method allows transformation or deserialization of the payload from its original type to a desired type,
     * as determined by the supplied {@code payloadType} parameter.
     *
     * @param <T> the target type of the message payload
     * @param message the reactive messaging {@code Message} to be transformed; must not be null
     * @param payloadType the {@code Class} object representing the target payload type; must not be null
     * @return a new {@code Message} instance with its payload transformed to the specified type
     */
    <T> org.eclipse.microprofile.reactive.messaging.Message<T> map(@NonNull Message message, @NonNull Class<T> payloadType);
}
