package io.quarkiverse.reactive.nats.jetstream.tracing.message;

import io.quarkiverse.reactive.nats.jetstream.message.Message;

/**
 * An interface for getting messaging attributes.
 */
public interface MessageInfo {

    default String getSystem(Message message) {
        return "jetstream";
    }

    String getDestination(Message message);

    Long getMessageBodySize(Message message);

    String getMessageId(Message message);

}
