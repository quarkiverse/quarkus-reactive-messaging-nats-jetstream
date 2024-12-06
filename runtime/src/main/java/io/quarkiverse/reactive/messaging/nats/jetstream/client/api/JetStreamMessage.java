package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;

public interface JetStreamMessage<T> extends Message<T>, ContextAwareMessage<T>, MetadataInjectableMessage<T> {
    String MESSAGE_TYPE_HEADER = "message.type";

    String messageId();

    Map<String, List<String>> headers();

}
