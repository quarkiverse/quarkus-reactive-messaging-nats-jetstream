package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import static io.quarkiverse.reactive.messaging.nats.jetstream.mapper.HeaderMapper.toMessageHeaders;
import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Metadata;

import io.nats.client.Message;
import io.smallrye.reactive.messaging.providers.helpers.VertxContext;
import io.vertx.mutiny.core.Context;

public class PublishMessage<T> implements JetStreamMessage<T> {
    private final Message message;
    private Metadata metadata;
    private final PublishMessageMetadata incomingMetadata;
    private final T payload;
    private final Context context;
    private final ExponentialBackoff exponentialBackoff;
    private final Duration ackTimeout;

    public PublishMessage(final Message message,
            final T payload,
            Context context,
            ExponentialBackoff exponentialBackoff,
            Duration ackTimeout) {
        this.message = message;
        this.incomingMetadata = PublishMessageMetadata.of(message);
        this.exponentialBackoff = exponentialBackoff;
        this.metadata = captureContextMetadata(incomingMetadata);
        this.payload = payload;
        this.context = context;
        this.ackTimeout = ackTimeout;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    public String messageId() {
        return incomingMetadata.messageId();
    }

    public byte[] getData() {
        return message.getData();
    }

    public String getSubject() {
        return incomingMetadata.subject();
    }

    public String getStream() {
        return incomingMetadata.stream();
    }

    public Long getStreamSequence() {
        return incomingMetadata.streamSequence();
    }

    public Long getConsumerSequence() {
        return incomingMetadata.consumerSequence();
    }

    public String getConsumer() {
        return incomingMetadata.consumer();
    }

    public Long getDeliveredCount() {
        return incomingMetadata.deliveredCount();
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
                message.ackSync(ackTimeout);
                this.runOnMessageContext(() -> f.complete(null));
            } catch (TimeoutException | InterruptedException e) {
                this.runOnMessageContext(() -> f.completeExceptionally(e));
            }
        });
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason, Metadata metadata) {
        return VertxContext.runOnContext(context.getDelegate(), f -> {
            if (exponentialBackoff.enabled()) {
                metadata.get(PublishMessageMetadata.class)
                        .ifPresentOrElse(m -> message.nakWithDelay(exponentialBackoff.getDuration(m.deliveredCount())),
                                message::nak);
            } else {
                message.nak();
            }
            this.runOnMessageContext(() -> f.complete(null));
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
    public String toString() {
        return "IncomingNatsMessage{" +
                "metadata=" + incomingMetadata +
                ", payload=" + payload +
                '}';
    }
}
