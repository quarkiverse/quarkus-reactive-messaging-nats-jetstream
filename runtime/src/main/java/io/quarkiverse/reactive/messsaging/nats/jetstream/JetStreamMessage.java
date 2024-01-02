package io.quarkiverse.reactive.messsaging.nats.jetstream;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;

public interface JetStreamMessage<T> extends Message<T>, ContextAwareMessage<T>, MetadataInjectableMessage<T> {

    String getMessageId();

    Map<String, List<String>> getHeaders();

}
