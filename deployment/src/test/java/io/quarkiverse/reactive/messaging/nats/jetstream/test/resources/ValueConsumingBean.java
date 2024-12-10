package io.quarkiverse.reactive.messaging.nats.jetstream.test.resources;

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
