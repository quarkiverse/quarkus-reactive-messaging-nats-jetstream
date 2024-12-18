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

public class ResolvedMessage<T> implements JetStreamMessage<T> {

    private static final Logger logger = Logger.getLogger(ResolvedMessage.class);

    private final MessageInfo message;
    private Metadata metadata;
    private final SubscribeMessageMetadata incomingMetadata;
    private final T payload;

    public ResolvedMessage(final MessageInfo message, final T payload) {
        this.message = message;
        this.incomingMetadata = SubscribeMessageMetadata.of(message);
        this.metadata = captureContextMetadata(incomingMetadata);
        this.payload = payload;
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

    public MessageInfo messageInfo() {
        return message;
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
        return CompletableFuture.supplyAsync(() -> {
            logger.debugf("Message with id = %s acknowledged", messageId());
            return null;
        });
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason, Metadata metadata) {
        return CompletableFuture.supplyAsync(() -> {
            logger.errorf(reason, "Message with id = %s not acknowledged", messageId());
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
