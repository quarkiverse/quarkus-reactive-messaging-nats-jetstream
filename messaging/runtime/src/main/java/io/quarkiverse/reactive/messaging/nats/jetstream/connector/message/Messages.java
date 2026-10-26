package io.quarkiverse.reactive.messaging.nats.jetstream.connector.message;

import java.util.ArrayList;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.ContextMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TraceContextMetadata;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;

/**
 * Converts between Reactive Messaging messages and the messages of the NATS client. Metadata items are carried over
 * as they are, except the Vert.x context ({@link LocalContextMetadata} and {@link ContextMetadata}) and the tracing
 * context ({@link TracingMetadata} and {@link TraceContextMetadata}), which are translated.
 */
public final class Messages {

    private Messages() {
    }

    /**
     * @param message a message received by the client; must not be null
     * @return the message as a Reactive Messaging message; acknowledging it acknowledges the received message
     */
    public static <T> @NonNull Message<T> incoming(
            io.quarkiverse.reactive.messaging.nats.jetstream.client.message.@NonNull Message<T> message) {
        return new IncomingMessage<>(message, toMessaging(message.getMetadata()));
    }

    /**
     * @param message a Reactive Messaging message to publish; must not be null
     * @return the message as a client message; acknowledging it acknowledges the Reactive Messaging message
     */
    public static <T> io.quarkiverse.reactive.messaging.nats.jetstream.client.message.@NonNull Message<T> outgoing(
            @NonNull Message<T> message) {
        return io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message.of(message.getPayload(),
                toClient(message.getMetadata()),
                metadata -> message.ack(toMessaging(metadata)),
                (reason, metadata) -> message.nack(reason, toMessaging(metadata)));
    }

    static io.quarkiverse.reactive.messaging.nats.jetstream.client.message.@NonNull Metadata toClient(
            @NonNull Metadata metadata) {
        final var items = new ArrayList<>();
        for (Object item : metadata) {
            if (item instanceof LocalContextMetadata localContext) {
                items.add(new ContextMetadata(localContext.context()));
            } else if (item instanceof TracingMetadata tracing) {
                if (tracing.getCurrentContext() != null) {
                    items.add(new TraceContextMetadata(tracing.getCurrentContext(), tracing.getPreviousContext()));
                }
            } else {
                items.add(item);
            }
        }
        return io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Metadata.from(items);
    }

    static @NonNull Metadata toMessaging(
            io.quarkiverse.reactive.messaging.nats.jetstream.client.message.@NonNull Metadata metadata) {
        final var items = new ArrayList<>();
        for (Object item : metadata) {
            if (item instanceof ContextMetadata contextMetadata) {
                items.add(new LocalContextMetadata(contextMetadata.context()));
            } else if (item instanceof TraceContextMetadata traceContext) {
                items.add(TracingMetadata.with(traceContext.current(), traceContext.previous()));
            } else {
                items.add(item);
            }
        }
        return Metadata.from(items);
    }
}
