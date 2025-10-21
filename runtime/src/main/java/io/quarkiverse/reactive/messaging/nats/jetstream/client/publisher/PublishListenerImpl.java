package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class PublishListenerImpl<T> implements PublishListener<T> {

    @Override
    public void onPublished(Message<T> message) {
        message.getMetadata(PublishMessageMetadata.class).ifPresent(metadata -> log
                .debugf("Published message with id: %s and sequence: %s", metadata.messageId(), metadata.sequence()));
    }

    @Override
    public void onError(Throwable throwable) {
        log.errorf(throwable, "An error occurred with message: %s", throwable.getMessage());
    }
}
