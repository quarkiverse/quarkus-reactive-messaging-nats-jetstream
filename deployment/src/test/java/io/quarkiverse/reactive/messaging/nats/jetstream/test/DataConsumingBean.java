package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamIncomingMessageMetadata;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.annotations.Blocking;

@ApplicationScoped
public class DataConsumingBean {
    private final static Logger logger = Logger.getLogger(DataConsumingBean.class);

    private AtomicReference<Data> lastData = new AtomicReference<>();

    @Blocking
    @Incoming("data-consumer")
    public Uni<Void> data(Message<String> message) {
        return Uni.createFrom().item(message)
                .onItem().invoke(m -> {
                    logger.infof("Received message: %s", message);
                    message.getMetadata(JetStreamIncomingMessageMetadata.class)
                            .ifPresent(metadata -> lastData.set(
                                    new Data(message.getPayload(), metadata.headers().get("RESOURCE_ID").get(0),
                                            metadata.messageId())));
                })
                .onItem().transformToUni(m -> Uni.createFrom().completionStage(m.ack()))
                .onFailure().recoverWithUni(throwable -> Uni.createFrom().completionStage(message.nack(throwable)));
    }

    public Optional<Data> getLast() {
        return Optional.ofNullable(lastData.get());
    }
}
