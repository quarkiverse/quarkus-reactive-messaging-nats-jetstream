package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Payload;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.vertx.mutiny.core.Context;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper(uses = PayloadMapper.class, componentModel = "cdi")
public interface MessageMapper {

    <T> List<Message<T>> map(List<io.nats.client.Message> messages, ConsumerConfiguration<T> configuration, Context context);

    <T> Message<T> map(io.nats.client.Message message, ConsumerConfiguration<T> configuration, Context context);

    default <T> Message<T> map(io.nats.client.Message message, ConsumerConfiguration<T> configuration, Context context, @org.mapstruct.Context PayloadMapper payloadMapper) {
        try {
            byte[] data = message.getData();
            Class<T> payloadType = configuration.payloadType().orElse(null);


            return new SubscribeMessage<>(message,
                    Optional.ofNullable(payloadMapper.map(data, payloadType)).map(Payload::data).orElse(null),
                    context,
                    configuration.acknowledgeTimeout(),
                    configuration.backoff().orElseGet(List::of));
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }
}
