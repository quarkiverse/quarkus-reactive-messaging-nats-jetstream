package io.quarkiverse.reactive.messaging.nats.jetstream.message;

import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;
import io.vertx.mutiny.core.Context;

public interface Message extends ContextAwareMessage<byte[]>, MetadataInjectableMessage<byte[]> {

    default Class<?> payloadType() {
        return getMetadata(Headers.class)
                .flatMap(Headers::payloadType)
                .orElseThrow(() -> new IllegalStateException("Payload type not found"));
    }

    static Message of(NativeMessage message, Context context) {
        return new VertxMessage(message, context);
    }

    static Message of(org.eclipse.microprofile.reactive.messaging.Message<byte[]> message, Headers headers) {
        if (message.getMetadata(LocalContextMetadata.class).isPresent()) {
            return new MessageDelegate(message.addMetadata(headers));
        } else {
            return new MessageDelegate(message.withMetadata(captureContextMetadata()).addMetadata(headers));
        }
    }
}
