package io.quarkiverse.reactive.messaging.nats.jetstream.message;

import io.vertx.core.Context;

public interface Message extends org.eclipse.microprofile.reactive.messaging.Message<byte[]> {

    static <P> Message<P> of(NativeMessage message, Context context, P payload) {
        return new VertxMessage<>(message, context, payload);
    }

}
