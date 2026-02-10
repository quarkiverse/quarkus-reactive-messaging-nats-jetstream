package io.quarkiverse.reactive.messaging.nats.jetstream.test.tls;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ValueConsumingBean {
    private static final Logger logger = Logger.getLogger(ValueConsumingBean.class);

    volatile long lastValue = -1;

    @Incoming("in")
    public void consume(long content) {
        logger.infof("Received message: %d", content);
        if (content > lastValue) {
            lastValue = content;
        }
    }

    public long getLastValue() {
        return lastValue;
    }
}
