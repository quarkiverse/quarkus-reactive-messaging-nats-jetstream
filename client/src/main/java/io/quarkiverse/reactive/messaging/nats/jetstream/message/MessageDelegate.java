package io.quarkiverse.reactive.messaging.nats.jetstream.message;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Metadata;

import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class MessageDelegate implements Message {
    private org.eclipse.microprofile.reactive.messaging.Message<byte[]> delegate;

    @Override
    public void injectMetadata(Object metadataObject) {
        if (delegate instanceof MetadataInjectableMessage<byte[]> metadataInjectableMessage) {
            metadataInjectableMessage.injectMetadata(metadataObject);
        } else {
            delegate = delegate.addMetadata(metadataObject);
        }
    }

    @Override
    public byte[] getPayload() {
        return delegate.getPayload();
    }

    @Override
    public <P> org.eclipse.microprofile.reactive.messaging.Message<P> withPayload(P payload) {
        return delegate.withPayload(payload);
    }

    @Override
    public Message withMetadata(Iterable<Object> metadata) {
        return new MessageDelegate(delegate.withMetadata(metadata));
    }

    @Override
    public Message withMetadata(Metadata metadata) {
        return new MessageDelegate(delegate.withMetadata(metadata));
    }

    @Override
    public Message withAck(Supplier<CompletionStage<Void>> supplier) {
        return new MessageDelegate(delegate.withAck(supplier));
    }

    @Override
    public Message withAckWithMetadata(Function<Metadata, CompletionStage<Void>> supplier) {
        return new MessageDelegate(delegate.withAckWithMetadata(supplier));
    }

    @Override
    public Message withNack(Function<Throwable, CompletionStage<Void>> nack) {
        return new MessageDelegate(delegate.withNack(nack));
    }

    @Override
    public Message withNackWithMetadata(BiFunction<Throwable, Metadata, CompletionStage<Void>> nack) {
        return new MessageDelegate(delegate.withNackWithMetadata(nack));
    }

    @Override
    public Metadata getMetadata() {
        return delegate.getMetadata();
    }

    @Override
    public <M> Optional<M> getMetadata(Class<? extends M> clazz) {
        return delegate.getMetadata(clazz);
    }

    @Override
    public Supplier<CompletionStage<Void>> getAck() {
        return delegate.getAck();
    }

    @Override
    public Function<Metadata, CompletionStage<Void>> getAckWithMetadata() {
        return delegate.getAckWithMetadata();
    }

    @Override
    public Function<Throwable, CompletionStage<Void>> getNack() {
        return delegate.getNack();
    }

    @Override
    public BiFunction<Throwable, Metadata, CompletionStage<Void>> getNackWithMetadata() {
        return delegate.getNackWithMetadata();
    }

    @Override
    public CompletionStage<Void> ack() {
        return delegate.ack();
    }

    @Override
    public CompletionStage<Void> ack(Metadata metadata) {
        return delegate.ack(metadata);
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason) {
        return delegate.nack(reason);
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason, Metadata metadata) {
        return delegate.nack(reason, metadata);
    }

    @Override
    public <C> C unwrap(Class<C> unwrapType) {
        return delegate.unwrap(unwrapType);
    }

    @Override
    public org.eclipse.microprofile.reactive.messaging.Message<byte[]> addMetadata(Object metadata) {
        return delegate.addMetadata(metadata);
    }

    @Override
    public <R> org.eclipse.microprofile.reactive.messaging.Message<R> thenApply(
            Function<org.eclipse.microprofile.reactive.messaging.Message<byte[]>, org.eclipse.microprofile.reactive.messaging.Message<R>> modifier) {
        return delegate.thenApply(modifier);
    }
}
