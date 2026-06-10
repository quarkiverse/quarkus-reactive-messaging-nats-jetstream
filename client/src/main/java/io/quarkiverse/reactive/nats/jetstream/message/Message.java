package io.quarkiverse.reactive.nats.jetstream.message;

import java.util.Optional;

import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;

public interface Message extends org.eclipse.microprofile.reactive.messaging.Message<byte[]>, ContextAwareMessage<byte[]>,
        MetadataInjectableMessage<byte[]> {

    static Message of(org.eclipse.microprofile.reactive.messaging.Message<byte[]> message) {
        return (Message) message;
    }

    static Message of(NativeMessage message) {
        return null;
    }

    Optional<Status> status();
}
