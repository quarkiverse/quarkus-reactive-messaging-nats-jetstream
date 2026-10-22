package io.quarkiverse.reactive.messaging.nats.jetstream.connector.message;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jspecify.annotations.NonNull;

import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;

/**
 * A message received by the NATS client, as a Reactive Messaging message. It runs on the Vert.x context the client
 * gave it, and acknowledging it acknowledges the received message.
 */
final class IncomingMessage<T> implements ContextAwareMessage<T>, MetadataInjectableMessage<T> {
    private final io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message<T> message;
    private volatile Metadata metadata;

    IncomingMessage(io.quarkiverse.reactive.messaging.nats.jetstream.client.message.@NonNull Message<T> message,
            @NonNull Metadata metadata) {
        this.message = message;
        this.metadata = metadata;
    }

    @Override
    public T getPayload() {
        return message.getPayload();
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public synchronized void injectMetadata(Object metadataObject) {
        this.metadata = metadata.with(metadataObject);
    }

    @Override
    public Function<Metadata, CompletionStage<Void>> getAckWithMetadata() {
        return metadata -> message.ack(Messages.toClient(metadata));
    }

    @Override
    public BiFunction<Throwable, Metadata, CompletionStage<Void>> getNackWithMetadata() {
        return (reason, metadata) -> message.nack(reason, Messages.toClient(metadata));
    }
}
