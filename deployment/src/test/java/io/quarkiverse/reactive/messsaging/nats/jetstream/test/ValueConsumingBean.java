package io.quarkiverse.reactive.messsaging.nats.jetstream.test;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class ValueConsumingBean {

    volatile long lastValue = -1;

    @Incoming("in")
    public void consume(long content) {
        lastValue = content;
    }

    public long getLastValue() {
        return lastValue;
    }

}
