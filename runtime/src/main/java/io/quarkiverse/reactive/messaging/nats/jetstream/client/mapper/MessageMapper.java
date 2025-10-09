package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration;
import io.vertx.mutiny.core.Context;

public interface MessageMapper {

    <T> List<Message<T>> map(List<io.nats.client.Message> messages, ConsumerConfiguration<T> configuration, Context context);

    <T> Message<T> map(io.nats.client.Message message, ConsumerConfiguration<T> configuration, Context context);

}
