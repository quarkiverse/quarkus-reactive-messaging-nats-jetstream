package io.quarkiverse.reactive.messaging.nats.jetstream.test.resources;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.annotations.Blocking;

@ApplicationScoped
public class RedeliveryConsumingBean {
    volatile Integer lastValue = -1;

    @Incoming("unstable")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    @Blocking
    public Uni<Void> unstable(Message<Integer> message) {
        return Uni.createFrom().item(message)
                .onItem().transformToUni(m -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    final var metadata = message.getMetadata(PublishMessageMetadata.class)
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
