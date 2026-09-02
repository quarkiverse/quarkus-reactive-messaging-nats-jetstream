package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;
import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;

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
public interface Message extends ContextAwareMessage<byte[]>, MetadataInjectableMessage<byte[]> {

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
    static @NonNull Message of(@NonNull final NativeMessage message,
            @NonNull final MessageContext context,
            @NonNull final ConsumerConfiguration consumerConfiguration) {
        return new MessageImpl(message, context, consumerConfiguration);
    }

    /**
     * Creates a new {@code Message} instance from the specified reactive messaging {@code Message}
     * and {@code Headers}. This method ensures that the resulting {@code Message} includes
     * context metadata if present in the original {@code Message}.
     *
     * @param message the reactive messaging {@code Message} instance containing the original payload and metadata; must not be
     *        null
     * @param headers the {@code Headers} instance to be added to the resulting {@code Message}; must not be null
     * @return a new {@code Message} instance that encapsulates the original payload, metadata, and additional headers
     */
    static @NonNull Message of(final org.eclipse.microprofile.reactive.messaging.@NonNull Message<byte[]> message,
            @NonNull final Headers headers) {
        if (message.getMetadata(LocalContextMetadata.class).isPresent()) {
            return new MessageDelegate(Message.of(message.getPayload(), message.getMetadata().with(headers)));
        } else {
            return new MessageDelegate(
                    Message.of(message.getPayload(), captureContextMetadata(message.getMetadata()).with(headers)));
        }
    }

    /**
     * Creates a new {@code Message} instance using the specified {@code payload}
     * and {@code headers}. This method ensures that the resulting {@code Message}
     * encapsulates the given payload and headers while delegating actual
     * construction to another {@code of} method.
     *
     * @param payload the byte array containing the payload for the message; must not be null
     * @param headers the {@code Headers} instance to be associated with the message; must not be null
     * @return a new {@code Message} instance containing the specified payload and headers
     */
    static @NonNull Message of(byte @NonNull [] payload,
            @NonNull final Headers headers) {
        return of(org.eclipse.microprofile.reactive.messaging.Message.of(payload), headers);
    }

    /**
     * Creates a new {@code Message} instance using the specified {@code payload}
     * and {@code metadata}. This method constructs a message that encapsulates
     * the provided payload and metadata, delegating the instantiation to a
     * {@code MessageDelegate}.
     *
     * @param payload the byte array containing the payload for the message; must not be null
     * @param metadata the {@code Metadata} instance to be associated with the message; must not be null
     * @return a new {@code Message} instance encapsulating the specified payload and metadata
     */
    static @NonNull Message of(byte @NonNull [] payload, @NonNull final Metadata metadata) {
        return new MessageDelegate(org.eclipse.microprofile.reactive.messaging.Message.of(payload, metadata));
    }

    /**
     * @see org.eclipse.microprofile.reactive.messaging.Message#addMetadata(Object)
     */
    @Override
    Message addMetadata(Object metadata);

    /**
     * @see org.eclipse.microprofile.reactive.messaging.Message#withMetadata(Iterable)
     */
    @Override
    Message withMetadata(Iterable<Object> metadata);

    /**
     * @see org.eclipse.microprofile.reactive.messaging.Message#withMetadata(Metadata)
     */
    Message withMetadata(org.eclipse.microprofile.reactive.messaging.Metadata metadata);

    /**
     * @see org.eclipse.microprofile.reactive.messaging.Message#withAck(Supplier)
     */
    @Override
    Message withAck(Supplier<CompletionStage<Void>> supplier);

    /**
     * @see org.eclipse.microprofile.reactive.messaging.Message#withAckWithMetadata(Function)
     */
    @Override
    Message withAckWithMetadata(Function<org.eclipse.microprofile.reactive.messaging.Metadata, CompletionStage<Void>> supplier);

    /**
     * @see org.eclipse.microprofile.reactive.messaging.Message#withNack(Function)
     */
    @Override
    Message withNack(Function<Throwable, CompletionStage<Void>> nack);

    /**
     * @see org.eclipse.microprofile.reactive.messaging.Message#withNackWithMetadata(BiFunction)
     */
    @Override
    Message withNackWithMetadata(BiFunction<Throwable, Metadata, CompletionStage<Void>> nack);

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
