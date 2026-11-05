package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.concurrency;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ConcurrencyConsumingBean {
    private final static Logger logger = Logger.getLogger(ConcurrencyConsumingBean.class);

    private final List<Integer> values = new CopyOnWriteArrayList<>();

    public List<Integer> getValues() {
        return List.copyOf(values);
    }

    @Incoming("concurrency-in")
    public Uni<Void> values(Message<Integer> message) {
        return Uni.createFrom().item(message)
                .onItem().transformToUni(m -> Uni.createFrom().item(() -> {
                    logger.infof("Received message on concurrency-in channel: %s", message);
                    values.add(message.getPayload());
                    return m;
                }))
                .onItem().transformToUni(m -> Uni.createFrom().completionStage(m.ack()))
                .onFailure().recoverWithUni(throwable -> Uni.createFrom().completionStage(message.nack(throwable)));
    }
}
