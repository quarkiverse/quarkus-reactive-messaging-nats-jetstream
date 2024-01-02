package io.quarkiverse.reactive.messsaging.nats.jetstream.test;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messsaging.nats.jetstream.JetStreamIncomingMessageMetadata;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.annotations.Blocking;

@ApplicationScoped
public class RedeliveryConsumingBean {
    volatile Integer lastValue = -1;

    @Incoming("unstable")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    @Blocking
    public Uni<Void> unstable(Message<Integer> message) {
        return Uni.createFrom().item(message)
                .onItem().invoke(m -> {
                    final var metadata = message.getMetadata(JetStreamIncomingMessageMetadata.class)
                            .orElseThrow(() -> new RuntimeException("No metadata"));
                    if (metadata.deliveredCount() < 3) {
                        message.nack(new Exception("Redeliver message"));
                    } else {
                        lastValue = message.getPayload();
                        message.ack();
                    }
                })
                .onFailure().invoke(message::nack)
                .onItem().ignore().andContinueWithNull();
    }

    public Integer getLast() {
        return lastValue;
    }
}
