package io.quarkiverse.reactive.nats.jetstream.tracing.message;

import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.quarkiverse.reactive.nats.jetstream.message.PublishMetadata;

public class PublishMessageInfo implements MessageInfo {

    @Override
    public String getDestination(Message message) {
        return message.getMetadata(PublishMetadata.class)
                .map(metadata -> String.format("%s.%s", metadata.stream(), metadata.subject()))
                .orElseThrow(() -> new IllegalArgumentException("Missing publish metadata"));
    }

    @Override
    public Long getMessageBodySize(Message message) {
        return (long) message.getPayload().length;
    }

    @Override
    public String getMessageId(Message message) {
        return message.getMetadata(PublishMetadata.class)
                .map(PublishMetadata::messageId).orElseThrow(() -> new IllegalArgumentException("Missing publish metadata"));
    }
}
