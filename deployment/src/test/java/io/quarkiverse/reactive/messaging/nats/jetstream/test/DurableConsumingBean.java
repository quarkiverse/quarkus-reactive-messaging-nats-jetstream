package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class DurableConsumingBean {
    private final static Logger logger = Logger.getLogger(DurableConsumingBean.class);

    private final AtomicReference<List<Integer>> values = new AtomicReference<>(new ArrayList<>());

    public List<Integer> getValues() {
        return values.get();
    }

    @Incoming("durable-consumer-1")
    public Uni<Void> durableConsumer1(Message<Integer> message) {
        return Uni.createFrom().item(message)
                .onItem().transformToUni(m -> Uni.createFrom().item(() -> {
                    logger.infof("Received message on durable-consumer-1 channel: %s", message);
                    values.updateAndGet(values -> {
                        values.add(message.getPayload());
                        return values;
                    });
                    return m;
                }))
                .onItem().transformToUni(m -> Uni.createFrom().completionStage(m.ack()))
                .onFailure().recoverWithUni(throwable -> Uni.createFrom().completionStage(message.nack(throwable)));
    }

    @Incoming("durable-consumer-2")
    public Uni<Void> durableConsumer2(Message<Integer> message) {
        return Uni.createFrom().item(message)
                .onItem().transformToUni(m -> Uni.createFrom().item(() -> {
                    logger.infof("Received message on durable-consumer-2 channel: %s", message);
                    values.updateAndGet(values -> {
                        values.add(message.getPayload());
                        return values;
                    });
                    return m;
                }))
                .onItem().transformToUni(m -> Uni.createFrom().completionStage(m.ack()))
                .onFailure().recoverWithUni(throwable -> Uni.createFrom().completionStage(message.nack(throwable)));
    }
}
