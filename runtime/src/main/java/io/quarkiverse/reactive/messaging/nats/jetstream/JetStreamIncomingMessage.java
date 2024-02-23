package io.quarkiverse.reactive.messaging.nats.jetstream;

import static io.quarkiverse.reactive.messaging.nats.jetstream.mapper.HeaderMapper.toMessageHeaders;
import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Metadata;

import io.nats.client.Message;
import io.smallrye.reactive.messaging.providers.helpers.VertxContext;
import io.vertx.mutiny.core.Context;

public class JetStreamIncomingMessage<T> implements JetStreamMessage<T> {
    private final Message message;
    private Metadata metadata;
    private final JetStreamIncomingMessageMetadata incomingMetadata;
    private final T payload;
    private final Context context;
    private final boolean exponentialBackoff;

    public JetStreamIncomingMessage(final Message message, final T payload, Context context, boolean exponentialBackoff) {
        this.message = message;
        this.incomingMetadata = JetStreamIncomingMessageMetadata.create(message);
        this.exponentialBackoff = exponentialBackoff;
        this.metadata = captureContextMetadata(incomingMetadata);
        this.payload = payload;
        this.context = context;
    }

    @Override
    public Metadata getMetadata() {
        return metadata;
    }

    public String getMessageId() {
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

    public Map<String, List<String>> getHeaders() {
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
            message.ack();
            this.runOnMessageContext(() -> f.complete(null));
        });
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason, Metadata metadata) {
        return VertxContext.runOnContext(context.getDelegate(), f -> {
            if (exponentialBackoff) {
                metadata.get(JetStreamIncomingMessageMetadata.class)
                        .ifPresentOrElse(m -> {
                            long backoffSeconds = Math.round(Math.pow(2D, m.deliveredCount()));
                            message.nakWithDelay(Duration.ofSeconds(backoffSeconds));
                        }, message::nak);
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
