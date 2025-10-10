package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import static io.nats.client.support.NatsJetStreamConstants.MSG_ID_HDR;
import static io.quarkiverse.reactive.messaging.nats.jetstream.client.api.JetStreamMessage.MESSAGE_TYPE_HEADER;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;

import io.nats.client.impl.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class PayloadMapperImpl implements PayloadMapper {
    private final Serializer serializer;
    private final HeaderMapper headerMapper;

    @Override
    public <T> SerializedPayload<T> map(Payload<T, T> payload) {
        return GenericSerializedPayload.<T> builder()
                .id(payload.id())
                .data(serializer.toBytes(payload.data()))
                .type(payload.type())
                .headers(payload.headers())
                .build();
    }

    @Override
    public <T> Payload<T, T> map(SerializedPayload<T> payload) {
        return GenericPayload.<T, T> builder()
                .id(payload.id())
                .type(payload.type())
                .data(serializer.readValue(payload.data(), payload.type()))
                .headers(payload.headers())
                .build();
    }

    @SuppressWarnings({ "DuplicatedCode", "unchecked" })
    @Override
    public <T> Payload<T, T> map(io.nats.client.Message message) {
        Class<T> type = getType(message.getHeaders())
                .map(clazz -> (Class<T>) clazz)
                .orElseThrow(
                        () -> new IllegalArgumentException("Message is missing the required header: " + MESSAGE_TYPE_HEADER));
        return GenericPayload.<T, T> builder()
                .data(serializer.readValue(message.getData(), type))
                .type(type)
                .headers(headerMapper.map(message.getHeaders()))
                .id(message.getHeaders().getFirst(MSG_ID_HDR))
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Payload<T, T> map(io.nats.client.Message message, Class<T> payLoadType) {
        Class<T> type = Optional.ofNullable(payLoadType).orElseGet(() -> getType(message.getHeaders())
                .map(clazz -> (Class<T>) clazz)
                .orElseThrow(
                        () -> new IllegalArgumentException("Message is missing the required header: " + MESSAGE_TYPE_HEADER)));
        return GenericPayload.<T, T> builder()
                .data(serializer.readValue(message.getData(), type))
                .type(type)
                .headers(headerMapper.map(message.getHeaders()))
                .id(message.getHeaders().getFirst(MSG_ID_HDR))
                .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Payload<T, T> map(org.eclipse.microprofile.reactive.messaging.Message<T> message) {
        final var metadata = message.getMetadata(PublishMessageMetadata.class);
        return GenericPayload.<T, T> builder()
                .data(message.getPayload())
                .type((Class<T>) message.getPayload().getClass())
                .headers(metadata.flatMap(publishMessageMetadata -> Optional.ofNullable(publishMessageMetadata.payload()))
                        .flatMap(payload -> Optional.ofNullable(payload.headers())).orElseGet(Map::of))
                .id(metadata.flatMap(publishMessageMetadata -> Optional.ofNullable(publishMessageMetadata.payload()))
                        .flatMap(payload -> Optional.ofNullable(payload.id())).orElseGet(() -> UUID.randomUUID().toString()))
                .build();
    }

    @SuppressWarnings({ "DuplicatedCode", "unchecked" })
    @Override
    public <T> Payload<T, T> map(io.nats.client.api.MessageInfo message) {
        Class<T> type = getType(message.getHeaders())
                .map(clazz -> (Class<T>) clazz)
                .orElseThrow(
                        () -> new IllegalArgumentException("Message is missing the required header: " + MESSAGE_TYPE_HEADER));
        return GenericPayload.<T, T> builder()
                .data(serializer.readValue(message.getData(), type))
                .type(type)
                .headers(headerMapper.map(message.getHeaders()))
                .id(message.getHeaders() != null ? message.getHeaders().getFirst(MSG_ID_HDR) : null)
                .build();
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> loadClass(String type) {
        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return (Class<T>) classLoader.loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> Optional<Class<T>> getType(Headers headers) {
        return Optional.ofNullable(headers).flatMap(h -> Optional.ofNullable(h.getFirst(MESSAGE_TYPE_HEADER)))
                .map(this::loadClass);
    }
}
