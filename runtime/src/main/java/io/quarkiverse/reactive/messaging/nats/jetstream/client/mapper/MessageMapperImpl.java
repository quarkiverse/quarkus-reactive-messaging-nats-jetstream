package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Payload;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration;
import io.vertx.mutiny.core.Context;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class MessageMapperImpl implements MessageMapper {
    private final PayloadMapper payloadMapper;

    @Override
    public <T> List<Message<T>> map(List<io.nats.client.Message> messages, ConsumerConfiguration configuration,
            Context context, Class<T> payloadType) {
        return messages.stream().map(message -> map(message, configuration, context, payloadType)).toList();
    }

    @Override
    public <T> Message<T> map(io.nats.client.Message message, ConsumerConfiguration configuration, Context context,
            Class<T> payloadType) {
        try {
            Payload<T, T> payload = Optional.ofNullable(payloadType).map(type -> payloadMapper.map(message, type))
                    .orElseGet(() -> payloadMapper.map(message));
            return new SubscribeMessage<>(message,
                    payload,
                    context,
                    configuration.acknowledgeTimeout(),
                    configuration.backoff().orElseGet(List::of));
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

}
