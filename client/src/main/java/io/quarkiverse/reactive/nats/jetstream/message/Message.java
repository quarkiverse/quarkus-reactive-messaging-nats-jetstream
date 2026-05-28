package io.quarkiverse.reactive.nats.jetstream.message;

import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;

import java.util.Optional;

public interface Message extends org.eclipse.microprofile.reactive.messaging.Message<byte[]>, ContextAwareMessage<byte[]>, MetadataInjectableMessage<byte[]> {

    static Message of(NativeMessage message) {
        return null;
    }

    Optional<Status> status();
}
