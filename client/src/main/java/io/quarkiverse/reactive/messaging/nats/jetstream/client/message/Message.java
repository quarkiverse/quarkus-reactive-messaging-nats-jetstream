package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration;
import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;
import io.vertx.mutiny.core.Context;

public interface Message extends ContextAwareMessage<byte[]>, MetadataInjectableMessage<byte[]> {

    default @NonNull Optional<Class<?>> payloadType() {
        return getMetadata(Headers.class)
                .flatMap(Headers::payloadType);
    }

    static @NonNull Message of(@NonNull final NativeMessage message, @NonNull final Context context,
            @NonNull final ConsumerConfiguration consumerConfiguration) {
        return new VertxMessage(message, context, consumerConfiguration);
    }

    static @NonNull Message of(final org.eclipse.microprofile.reactive.messaging.@NonNull Message<byte[]> message,
            @NonNull final Headers headers) {
        if (message.getMetadata(LocalContextMetadata.class).isPresent()) {
            return new MessageDelegate(message.addMetadata(headers));
        } else {
            return new MessageDelegate(message.withMetadata(captureContextMetadata()).addMetadata(headers));
        }
    }
}
