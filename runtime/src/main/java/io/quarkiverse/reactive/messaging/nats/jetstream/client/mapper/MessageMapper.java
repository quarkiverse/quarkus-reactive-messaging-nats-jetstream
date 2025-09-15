package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.vertx.mutiny.core.Context;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface MessageMapper {

    <T> @NonNull List<Message<T>> of(@NonNull List<io.nats.client.Message> messages, @NonNull ConsumerConfiguration<T> configuration, @NonNull Context context);

    <T> @NonNull Message<T> of(io.nats.client.Message message, @NonNull ConsumerConfiguration<T> configuration, @NonNull Context context);
}
