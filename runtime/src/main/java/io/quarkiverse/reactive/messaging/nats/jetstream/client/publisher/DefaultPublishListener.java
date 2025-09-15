package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

import io.nats.client.Connection;
import lombok.extern.jbosslog.JBossLog;
import org.jspecify.annotations.NonNull;

@JBossLog
public class DefaultPublishListener implements PublishListener {

    @Override
    public void connectionEvent(Connection connection, Events type) {
        log.debugf("NATS connection event: %s", type);
    }

    @Override
    public void onPublished(@NonNull String stream, @NonNull String subject, @NonNull Long sequence) {
        log.infof("Published message with sequence: %s to stream: %s and subject: %s", sequence, stream, subject);
    }

    @Override
    public void onError(@NonNull String stream, @NonNull String subject, @NonNull Throwable throwable) {
        log.errorf("Error publishing message to stream: %s and subject: %s with message: %s", stream, subject, throwable.getMessage());
    }
}
