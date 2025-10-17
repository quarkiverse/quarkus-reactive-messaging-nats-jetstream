package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Metadata;

public interface MessageConsumer<T> {

    default Uni<Void> acknowledge(Message<T> message) {
        return Uni.createFrom().completionStage(message.ack());
    }

    default Uni<Void> notAcknowledge(Message<T> message, Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(throwable));
    }

    default Uni<Void> notAcknowledge(Message<T> message, Throwable throwable, Metadata metadata) {
        return Uni.createFrom().completionStage(message.nack(throwable, metadata));
    }
}
