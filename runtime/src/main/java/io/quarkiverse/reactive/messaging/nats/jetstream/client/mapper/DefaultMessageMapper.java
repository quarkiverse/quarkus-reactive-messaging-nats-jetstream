package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkus.arc.DefaultBean;
import io.vertx.mutiny.core.Context;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;

@ApplicationScoped
@DefaultBean
@RequiredArgsConstructor
public class DefaultMessageMapper implements MessageMapper {
    private final PayloadMapper payloadMapper;


    @Override
    public <T> List<Message<T>> of(List<io.nats.client.Message> messages, ConsumerConfiguration<T> configuration, Context context) {
        return messages.stream().map(message -> of(message, configuration, context)).toList();
    }

    @Override
    public <T> Message<T> of(io.nats.client.Message message, ConsumerConfiguration<T> configuration, Context context) {
        try {
            return new SubscribeMessage<>(message,
                    payloadMapper.of(message, configuration.payloadType().orElse(null)),
                    context,
                    configuration.acknowledgeTimeout(),
                    configuration.backoff().orElseGet(List::of));
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }
}
