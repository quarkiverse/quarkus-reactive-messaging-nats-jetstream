package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.smallrye.mutiny.Uni;

/**
 * Represents a publisher capable of publishing messages to specific streams and subjects.
 * This interface defines a single operation for asynchronously publishing messages.
 */
public interface Publisher {

    /**
     * Publishes a message to a specified stream and subject.
     *
     * @param message the message to be published, must not be null
     * @param stream the name of the target stream, must not be null
     * @param subject the target subject within the stream, must not be null
     * @return a {@link Uni} that resolves to the published {@link Message} upon successful completion
     */
    @NonNull
    <T> Uni<Message<T>> publish(@NonNull Message<T> message, @NonNull String stream, @NonNull String subject);

}
