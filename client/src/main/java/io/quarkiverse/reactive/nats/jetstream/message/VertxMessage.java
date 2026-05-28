package io.quarkiverse.reactive.nats.jetstream.message;

import io.smallrye.reactive.messaging.providers.helpers.VertxContext;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;
import io.vertx.core.Context;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

final class VertxMessage implements Message {
    private final io.nats.client.Message message;
    private org.eclipse.microprofile.reactive.messaging.Metadata metadata;
    private final Context context;

    public VertxMessage(io.nats.client.Message message, Metadata metadata, Context context) {
        this.message = message;
        this.metadata = captureContextMetadata(metadata);
        this.context = context;
    }

    @Override
    public org.eclipse.microprofile.reactive.messaging.Metadata getMetadata() {
        return metadata;
    }

    @Override
    public synchronized void injectMetadata(Object metadataObject) {
        this.metadata = metadata.with(metadataObject);
    }

    @Override
    public byte[] getPayload() {
        return message.getData();
    }

    @Override
    public Optional<Status> status() {
        return message.isStatusMessage() ? Optional.of(Status.of(message.getStatus())) : Optional.empty();
    }

    @Override
    public Supplier<CompletionStage<Void>> getAck() {
        return this::ack;
    }

    @Override
    public CompletionStage<Void> ack() {
        return VertxContext.runOnContext(context, f -> {
            try {
                final var messageMetadata = metadata.get(Metadata.class);
                messageMetadata.flatMap(Metadata::acknowledgeTimeout)
                        .ifPresentOrElse(timeout -> {
                            try {
                                message.ackSync(timeout);
                            } catch (TimeoutException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }, message::ack);
                this.runOnMessageContext(() -> f.complete(null));
            } catch (Exception e) {
                this.runOnMessageContext(() -> f.completeExceptionally(e));
            }
        });
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason, org.eclipse.microprofile.reactive.messaging.Metadata metadata) {
        return VertxContext.runOnContext(context, f -> {
            try {
                final var messageMetadata = metadata.get(Metadata.class);
                messageMetadata.flatMap(this::getBackoff).ifPresentOrElse(message::nakWithDelay, message::nak);
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
    public BiFunction<Throwable, org.eclipse.microprofile.reactive.messaging.Metadata, CompletionStage<Void>> getNackWithMetadata() {
        return this::nack;
    }

    @Override
    public Optional<LocalContextMetadata> getContextMetadata() {
        return metadata.get(LocalContextMetadata.class);
    }

    @Override
    public org.eclipse.microprofile.reactive.messaging.Message<byte[]> addMetadata(Object metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    @Override
    public org.eclipse.microprofile.reactive.messaging.Message<byte[]> withMetadata(Iterable<Object> metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    @Override
    public org.eclipse.microprofile.reactive.messaging.Message<byte[]> withMetadata(org.eclipse.microprofile.reactive.messaging.Metadata metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    private Optional<Duration> getBackoff(Metadata metadata) {
        if (metadata.acknowledgeBackoff().isEmpty()) {
            return Optional.empty();
        } else if (metadata.deliveredCount() == 0) {
            return Optional.of(metadata.acknowledgeBackoff().getFirst());
        } else if (metadata.deliveredCount() >= metadata.acknowledgeBackoff().size()) {
            return Optional.of(metadata.acknowledgeBackoff().getLast());
        } else {
            return Optional.of(metadata.acknowledgeBackoff().get(metadata.deliveredCount() - 1));
        }
    }
}
