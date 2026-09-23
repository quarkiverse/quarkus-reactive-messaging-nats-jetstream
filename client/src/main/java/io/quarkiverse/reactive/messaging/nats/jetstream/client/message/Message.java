package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Context;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;
import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;

/**
 * Represents a message abstraction that extends capabilities for providing context-aware functionality
 * and metadata injection. This interface serves as a common contract for handling messages with
 * various features such as payload manipulation, metadata addition, acknowledgment, and error handling.
 * The {@code Message} interface is designed to work with byte array payloads and integrates with
 * reactive messaging systems, making it suitable for distributed event-driven systems.
 * This interface provides several factory methods to create instances of {@code Message} from different
 * sources such as {@link NativeMessage} or reactive messaging frameworks.
 * Implementations of this interface should ensure proper processing, serialization, and interaction with
 * contextual and metadata-aware aspects.
 */
public interface Message<T> extends ContextAwareMessage<T>, MetadataInjectableMessage<T> {

    /**
     * Creates a new {@code Message} instance from the specified {@code NativeMessage}, {@code MessageContext},
     * and {@code ConsumerConfiguration}.
     *
     * @param message the {@code NativeMessage} from which the {@code Message} instance is created; must not be null
     * @param context the {@code MessageContext} providing execution context for the message; must not be null
     * @param consumerConfiguration the {@code ConsumerConfiguration} defining configurations for the message consumer; must not
     *        be null
     * @return a {@code Message} instance that encapsulates the given {@code NativeMessage}, context, and consumer configuration
     */
    static @NonNull Message<byte[]> of(@NonNull final NativeMessage message,
            @NonNull final Context context,
            @NonNull final ConsumerConfiguration consumerConfiguration) {
        return new MessageImpl<>(message, message.getData(), context, consumerConfiguration);
    }

    /**
     * Creates a new {@code Message} instance from the specified {@code NativeMessage}, {@code MessageContext},
     * payload, and {@code Metadata}.
     *
     * @param <T> the type of the payload
     * @param message the {@code NativeMessage} from which the {@code Message} instance is created; must not be null
     * @param context the {@code MessageContext} providing execution context for the message; must not be null
     * @param payload the optional payload to be included in the message; may be null
     * @param metadata the {@code Metadata} instance associated with the message; must not be null
     * @return a new {@code Message} instance that encapsulates the given {@code NativeMessage}, context, payload, and metadata
     */
    static <T> @NonNull Message<T> of(@NonNull final NativeMessage message,
            @NonNull final Context context,
            @Nullable final T payload,
            org.eclipse.microprofile.reactive.messaging.@NonNull Metadata metadata) {
        return new MessageImpl<>(message, payload, context, metadata);
    }

    default List<? extends Headers> getHeaders() {
        final var headers = new ArrayList<Headers>();
        for (final var metadata : getMetadata()) {
            if (metadata instanceof Headers) {
                headers.add((Headers) metadata);
            }
        }
        return headers;
    }
}
