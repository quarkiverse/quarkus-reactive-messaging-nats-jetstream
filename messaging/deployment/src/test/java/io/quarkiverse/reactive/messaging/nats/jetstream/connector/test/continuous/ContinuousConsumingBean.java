package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.continuous;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ContinuousConsumingBean {
    private final static Logger logger = Logger.getLogger(ContinuousConsumingBean.class);

    private final AtomicReference<List<Integer>> values = new AtomicReference<>(new ArrayList<>());
    private final AtomicReference<List<Integer>> typedValues = new AtomicReference<>(new ArrayList<>());

    public List<Integer> getValues() {
        return values.get();
    }

    public List<Integer> getTypedValues() {
        return typedValues.get();
    }

    @Incoming("continuous-in")
    public Uni<Void> values(Message<Integer> message) {
        return Uni.createFrom().item(message)
                .onItem().transformToUni(m -> Uni.createFrom().item(() -> {
                    logger.infof("Received message on continuous-in channel: %s", message);
                    collect(values, message.getPayload());
                    return m;
                }))
                .onItem().transformToUni(m -> Uni.createFrom().completionStage(m.ack()))
                .onFailure().recoverWithUni(throwable -> Uni.createFrom().completionStage(message.nack(throwable)));
    }

    @Incoming("continuous-typed-in")
    public Uni<Void> typedValues(Message<Integer> message) {
        return Uni.createFrom().item(message)
                .onItem().transformToUni(m -> Uni.createFrom().item(() -> {
                    logger.infof("Received message on continuous-typed-in channel: %s", message);
                    collect(typedValues, message.getPayload());
                    return m;
                }))
                .onItem().transformToUni(m -> Uni.createFrom().completionStage(m.ack()))
                .onFailure().recoverWithUni(throwable -> Uni.createFrom().completionStage(message.nack(throwable)));
    }

    private void collect(AtomicReference<List<Integer>> collected, Integer value) {
        collected.updateAndGet(current -> {
            current.add(value);
            return current;
        });
    }
}
