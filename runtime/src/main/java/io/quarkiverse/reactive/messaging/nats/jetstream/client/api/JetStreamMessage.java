package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import static io.quarkiverse.reactive.messaging.nats.jetstream.mapper.HeaderMapper.toMessageHeaders;
import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jboss.logging.Logger;

import io.nats.client.api.MessageInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamIncomingMessageMetadata;

public class JetStreamMessage<T> implements io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamMessage<T> {

    private static final Logger logger = Logger.getLogger(JetStreamMessage.class);

    private final MessageInfo message;
    private Metadata metadata;
    private final JetStreamIncomingMessageMetadata incomingMetadata;
    private final T payload;

    public JetStreamMessage(final MessageInfo message, final T payload) {
        this.message = message;
        this.incomingMetadata = JetStreamIncomingMessageMetadata.of(message);
        this.metadata = captureContextMetadata(incomingMetadata);
        this.payload = payload;
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
        return CompletableFuture.supplyAsync(() -> {
            logger.debugf("Message with id = %s acknowledged", getMessageId());
            return null;
        });
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason, Metadata metadata) {
        return CompletableFuture.supplyAsync(() -> {
            logger.errorf(reason, "Message with id = %s not acknowledged", getMessageId());
            throw new RuntimeException(reason);
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
    public org.eclipse.microprofile.reactive.messaging.Message<T> addMetadata(Object metadata) {
        injectMetadata(metadata);
        return this;
    }

    @Override
    public String toString() {
        return "JetStreamMessage{" +
                "message=" + message +
                ", metadata=" + metadata +
                ", incomingMetadata=" + incomingMetadata +
                ", payload=" + payload +
                '}';
    }

}
