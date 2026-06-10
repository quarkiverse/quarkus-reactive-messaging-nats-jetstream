package io.quarkiverse.reactive.messaging.nats.test.subscription;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.client.api.SubscribeMessageMetadata;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

@ApplicationScoped
public class RedeliveryConsumingBean {
    volatile Integer lastValue = -1;

    @Incoming("unstable")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Uni<Void> unstable(Message<Integer> message) {
        return Uni.createFrom().item(message)
                .onItem().transformToUni(m -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    final var metadata = message.getMetadata(SubscribeMessageMetadata.class)
                            .orElseThrow(() -> new RuntimeException("No metadata"));
                    if (metadata.deliveredCount() < 3) {
                        throw new RuntimeException("Redeliver message");
                    } else {
                        lastValue = message.getPayload();
                    }
                    return m;
                })))
                .onItem().transformToUni(m -> Uni.createFrom().completionStage(m.ack()))
                .onFailure().recoverWithUni(throwable -> Uni.createFrom().completionStage(message.nack(throwable)));
    }

    public Integer getLast() {
        return lastValue;
    }
}
