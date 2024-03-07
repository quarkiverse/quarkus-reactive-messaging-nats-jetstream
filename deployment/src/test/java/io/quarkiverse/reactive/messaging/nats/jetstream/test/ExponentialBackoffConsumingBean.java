package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.administration.MessageResolver;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ExponentialBackoffConsumingBean {
    private final static Logger logger = Logger.getLogger(ExponentialBackoffConsumingBean.class);

    private final AtomicReference<Map<Integer, Integer>> retries = new AtomicReference<>(new HashMap<>());
    private final AtomicReference<List<Integer>> maxDeliveries = new AtomicReference<>(new ArrayList<>());

    @Inject
    MessageResolver resolver;

    public int getNumOfRetries(int d) {
        return retries.get().getOrDefault(d, 0);
    }

    public List<Integer> maxDelivered() {
        return maxDeliveries.get();
    }

    @Incoming("exponential-backoff-consumer")
    public Uni<Void> exponentialBackoffConsumer(Message<Integer> message) {
        return Uni.createFrom().item(message)
                .onItem().invoke(() -> {
                    logger.infof("Received message on exponential-backoff-consumer: %s", message);
                    int r = retries.get().getOrDefault(message.getPayload(), 0);
                    retries.get().put(message.getPayload(), ++r);
                    message.nack(new RuntimeException("Failed to deliver"));
                })
                .onFailure().invoke(message::nack)
                .onItem().ignore().andContinueWithNull();
    }

    @Incoming("max-deliveries-consumer")
    public Uni<Void> maxDeliveries(Message<Advisory> message) {
        logger.infof("Received messge on max-deliveries-consumer channel: %s", message);
        final var advisory = message.getPayload();
        return resolver.<Integer> resolve(advisory.getStream(), advisory.getStream_seq())
                .onItem().invoke(msg -> {
                    maxDeliveries.get().add(msg.getPayload());
                    message.ack();
                })
                .onFailure().invoke(message::nack)
                .replaceWithVoid();
    }

}
