package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import io.smallrye.mutiny.Uni;

public interface MessageConsumer<T> {

    default Uni<Void> acknowledge(Message<T> message) {
        return Uni.createFrom().completionStage(message.ack());
    }

    default Uni<Void> acknowledge(io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message message) {
        return Uni.createFrom().completionStage(message.ack());
    }

    default Uni<Void> notAcknowledge(Message<T> message, Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(throwable));
    }

    default Uni<Void> notAcknowledge(io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message message,
            Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(throwable));
    }

    default Uni<Void> notAcknowledge(Message<?> message,
            Throwable throwable, Metadata metadata) {
        return Uni.createFrom().completionStage(message.nack(throwable, metadata));
    }
}
