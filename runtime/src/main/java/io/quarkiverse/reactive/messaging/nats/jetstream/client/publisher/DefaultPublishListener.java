package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

import lombok.extern.jbosslog.JBossLog;
import org.jspecify.annotations.NonNull;

@JBossLog
public class DefaultPublishListener implements PublishListener {

    @Override
    public void onConnected(io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection connection) {
        log.debugf("NATS connection established: %s", connection.getConnectedUrl());
    }


    @Override
    public void onPublished(@NonNull String stream, @NonNull String subject, @NonNull Long sequence) {
        log.infof("Published message with sequence: %s to stream: %s and subject: %s", sequence, stream, subject);
    }

    @Override
    public void onError(Throwable throwable) {
        log.errorf("Error publishing message with message: %s", throwable.getMessage());
    }
}
