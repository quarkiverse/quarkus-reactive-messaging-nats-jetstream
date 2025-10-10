package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import org.eclipse.microprofile.reactive.messaging.Message;

import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class ConsumerListenerImpl<T> implements ConsumerListener<T> {

    @Override
    public void onMessage(Message<T> message) {
        log.debugf("Received message: %s", message);
    }

    @Override
    public void onError(Throwable throwable) {
        log.errorf(throwable, "An error occurred with message: %s", throwable.getMessage());
    }
}
