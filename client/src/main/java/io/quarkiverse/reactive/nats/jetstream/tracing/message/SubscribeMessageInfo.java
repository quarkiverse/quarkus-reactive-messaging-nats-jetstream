package io.quarkiverse.reactive.nats.jetstream.tracing.message;

import io.quarkiverse.reactive.nats.jetstream.message.Message;

public class SubscribeMessageInfo implements MessageInfo {

    @Override
    public String getDestination(Message message) {

        return String.format("%s.%s", metadata.stream(), metadata.subject());
    }

    @Override
    public Long getMessageBodySize(Message message) {
        return (long) message.getPayload().length;
    }

    @Override
    public String getMessageId(Message message) {
        return metadata.messageId();
    }

}
