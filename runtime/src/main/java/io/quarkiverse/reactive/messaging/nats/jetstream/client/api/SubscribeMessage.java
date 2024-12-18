package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import static io.quarkiverse.reactive.messaging.nats.jetstream.mapper.HeaderMapper.toMessageHeaders;
import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Metadata;

import io.nats.client.Message;
import io.smallrye.reactive.messaging.providers.helpers.VertxContext;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;
import io.vertx.mutiny.core.Context;

public class SubscribeMessage<T> implements JetStreamMessage<T> {
    private final Message message;
    private Metadata metadata;
    private final SubscribeMessageMetadata subscribeMessageMetadata;
    private final T payload;
    private final Context context;

    public SubscribeMessage(final Message message,
            final T payload,
            Context context) {
        this.message = message;
        this.subscribeMessageMetadata = SubscribeMessageMetadata.of(message);
        this.metadata = captureContextMetadata(subscribeMessageMetadata);
        this.payload = payload;
        this.context = context;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    public String messageId() {
        return subscribeMessageMetadata.messageId();
    }

    public byte[] getData() {
        return message.getData();
    }

    public String getSubject() {
        return subscribeMessageMetadata.subject();
    }

    public String getStream() {
        return subscribeMessageMetadata.stream();
    }

    public Long getStreamSequence() {
        return subscribeMessageMetadata.streamSequence();
    }

    public Long getConsumerSequence() {
        return subscribeMessageMetadata.consumerSequence();
    }

    public String getConsumer() {
        return subscribeMessageMetadata.consumer();
    }

    public Long getDeliveredCount() {
        return subscribeMessageMetadata.deliveredCount();
    }

    public Map<String, List<String>> headers() {
        return toMessageHeaders(message.getHeaders());
    }

    @Override
    public T getPayload() {
        return payload;
    }

    @Override
    public Supplier<CompletionStage<Void>> getAck() {
        return this::ack;
    }

    @Override
    public CompletionStage<Void> ack() {
        return VertxContext.runOnContext(context.getDelegate(), f -> {
            try {
                message.ack();
                this.runOnMessageContext(() -> f.complete(null));
            } catch (Exception e) {
                this.runOnMessageContext(() -> f.completeExceptionally(e));
            }
        });
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason, Metadata metadata) {
        return VertxContext.runOnContext(context.getDelegate(), f -> {
            try {
                message.nak();
                this.runOnMessageContext(() -> f.complete(null));
            } catch (Exception e) {
                this.runOnMessageContext(() -> f.completeExceptionally(e));
            }
        });
    }

    @Override
    public Function<Throwable, CompletionStage<Void>> getNack() {
        return this::nack;
    }

    @Override
    public synchronized void injectMetadata(Object metadataObject) {
        this.metadata = metadata.with(metadataObject);
    }

    @Override
    public Optional<LocalContextMetadata> getContextMetadata() {
        return metadata.get(LocalContextMetadata.class);
    }

    @Override
    public org.eclipse.microprofile.reactive.messaging.Message<T> addMetadata(Object metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    @Override
    public org.eclipse.microprofile.reactive.messaging.Message<T> withMetadata(Iterable<Object> metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    @Override
    public org.eclipse.microprofile.reactive.messaging.Message<T> withMetadata(Metadata metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    @Override
    public String toString() {
        return "SubscribeMessage{" +
                "metadata=" + subscribeMessageMetadata +
                ", payload=" + payload +
                '}';
    }
}
