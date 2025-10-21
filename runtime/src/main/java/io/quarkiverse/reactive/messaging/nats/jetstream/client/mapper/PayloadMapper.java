package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Payload;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SerializedPayload;

public interface PayloadMapper {

    <T> SerializedPayload<T> map(Payload<T, T> payload);

    <T> Payload<T, T> map(SerializedPayload<T> payload);

    <T> Payload<T, T> map(io.nats.client.api.MessageInfo message);

    <T> Payload<T, T> map(io.nats.client.Message message, Class<T> payLoadType);

    <T> Payload<T, T> map(io.nats.client.Message message);

    <T> Payload<T, T> map(org.eclipse.microprofile.reactive.messaging.Message<T> message);

}
