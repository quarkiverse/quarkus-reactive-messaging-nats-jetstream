package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class DefaultErrorListener implements ErrorListener {

    @Override
    public void onError(Throwable throwable) {
        log.errorf(throwable,"An error occurred with message: %s", throwable.getMessage());
    }
}
