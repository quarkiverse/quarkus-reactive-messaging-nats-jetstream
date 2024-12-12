package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PublishConfiguration;
import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;
import lombok.Builder;

@Builder
public record SubscribeMessage<T>(String stream, String subject, byte[] payload, String type, String messageId,
        Map<String, List<String>> headers, Message<T> message) implements JetStreamMessage<T> {

    @Override
    public void injectMetadata(Object o) {
        if (message instanceof MetadataInjectableMessage<T> metadataInjectableMessage) {
            metadataInjectableMessage.injectMetadata(o);
        }
    }

    @Override
    public Metadata getMetadata() {
        return message.getMetadata();
    }

    @Override
    public Optional<LocalContextMetadata> getContextMetadata() {
        if (message instanceof ContextAwareMessage<T> contextAwareMessage) {
            return contextAwareMessage.getContextMetadata();
        }
        return Optional.empty();
    }

    @Override
    public void runOnMessageContext(Runnable runnable) {
        if (message instanceof ContextAwareMessage<T> contextAwareMessage) {
            contextAwareMessage.runOnMessageContext(runnable);
            ;
        }
    }

    @Override
    public <P> Message<P> withPayload(P payload) {
        try {
            final var obejectMapper = new ObjectMapper();
            return SubscribeMessage.<P> builder()
                    .stream(stream)
                    .subject(subject)
                    .payload(obejectMapper.writeValueAsBytes(payload))
                    .message(message.withPayload(payload))
                    .type(payload.getClass().getTypeName())
                    .messageId(messageId)
                    .headers(headers)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message<T> withMetadata(Iterable<Object> metadata) {
        return SubscribeMessage.of(message.withMetadata(metadata), this);
    }

    @Override
    public Message<T> withMetadata(Metadata metadata) {
        return SubscribeMessage.of(message.withMetadata(metadata), this);
    }

    @Override
    public Message<T> withAck(Supplier<CompletionStage<Void>> supplier) {
        return SubscribeMessage.of(message.withAck(supplier), this);
    }

    @Override
    public Message<T> withAckWithMetadata(Function<Metadata, CompletionStage<Void>> supplier) {
        return SubscribeMessage.of(message.withAckWithMetadata(supplier), this);
    }

    @Override
    public Message<T> withNack(Function<Throwable, CompletionStage<Void>> nack) {
        return SubscribeMessage.of(message.withNack(nack), this);
    }

    @Override
    public Message<T> withNackWithMetadata(BiFunction<Throwable, Metadata, CompletionStage<Void>> nack) {
        return SubscribeMessage.of(message.withNackWithMetadata(nack), this);
    }

    @Override
    public <M> Optional<M> getMetadata(Class<? extends M> clazz) {
        return message.getMetadata(clazz);
    }

    @Override
    public Supplier<CompletionStage<Void>> getAck() {
        return message.getAck();
    }

    @Override
    public Function<Metadata, CompletionStage<Void>> getAckWithMetadata() {
        return message.getAckWithMetadata();
    }

    @Override
    public Function<Throwable, CompletionStage<Void>> getNack() {
        return message.getNack();
    }

    @Override
    public BiFunction<Throwable, Metadata, CompletionStage<Void>> getNackWithMetadata() {
        return message.getNackWithMetadata();
    }

    @Override
    public CompletionStage<Void> ack() {
        return message.ack();
    }

    @Override
    public CompletionStage<Void> ack(Metadata metadata) {
        return message.ack(metadata);
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason) {
        return message.nack(reason);
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason, Metadata metadata) {
        return message.nack(reason, metadata);
    }

    @Override
    public <C> C unwrap(Class<C> unwrapType) {
        return message.unwrap(unwrapType);
    }

    @Override
    public Message<T> addMetadata(Object metadata) {
        return message.addMetadata(metadata);
    }

    @Override
    public <R> Message<R> thenApply(Function<Message<T>, Message<R>> modifier) {
        return message.thenApply(modifier);
    }

    @Override
    public T getPayload() {
        return message.getPayload();
    }

    public static <T> SubscribeMessage<T> of(Message<T> message, byte[] payload, PublishConfiguration configuration) {
        final var metadata = message.getMetadata(SubscribeMessageMetadata.class);
        final var stream = metadata.flatMap(SubscribeMessageMetadata::streamOptional).orElseGet(configuration::stream);
        final var subject = metadata.flatMap(SubscribeMessageMetadata::subjectOptional).orElseGet(configuration::subject);
        final var messageId = metadata.flatMap(SubscribeMessageMetadata::messageIdOptional)
                .orElseGet(() -> UUID.randomUUID().toString());
        final var type = message.getPayload() != null ? message.getPayload().getClass().getTypeName() : null;
        final var headers = metadata.flatMap(SubscribeMessageMetadata::headersOptional).map(HashMap::new)
                .orElseGet(HashMap::new);
        if (type != null) {
            headers.putIfAbsent(MESSAGE_TYPE_HEADER, List.of(type));
        }
        return SubscribeMessage.<T> builder()
                .stream(stream)
                .subject(subject)
                .payload(payload)
                .message(message)
                .type(type)
                .messageId(messageId)
                .headers(headers)
                .build();
    }

    private static <T> SubscribeMessage<T> of(Message<T> message, SubscribeMessage<T> subscribeMessage) {
        return SubscribeMessage.<T> builder()
                .stream(subscribeMessage.stream())
                .subject(subscribeMessage.subject())
                .payload(subscribeMessage.payload())
                .message(message)
                .type(subscribeMessage.type())
                .messageId(subscribeMessage.messageId())
                .headers(subscribeMessage.headers())
                .build();
    }

}
