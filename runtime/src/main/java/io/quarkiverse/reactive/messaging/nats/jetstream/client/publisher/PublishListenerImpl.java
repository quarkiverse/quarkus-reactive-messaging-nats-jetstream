package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class PublishListenerImpl implements PublishListener {

    @Override
    public void onPublished(String messageId, Long sequence) {
        log.infof("Published message with id: %s and sequence: %s", sequence);
    }

    @Override
    public void onError(Throwable throwable) {
        log.errorf(throwable, "An error occurred with message: %s", throwable.getMessage());
    }
}