package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.administration.MessageResolver;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class DeadLetterConsumingBean {
    private final static Logger logger = Logger.getLogger(DeadLetterConsumingBean.class);

    private final AtomicReference<Data> lastData = new AtomicReference<>();

    @Inject
    MessageResolver resolver;

    public Optional<Data> getLast() {
        return Optional.ofNullable(lastData.get());
    }

    @Incoming("unstable-data-consumer")
    public Uni<Void> durableConsumer(Message<Data> message) {
        return Uni.createFrom().item(message)
                .onItem().invoke(() -> {
                    logger.infof("Received message on unstable-data-consumer channel: %s", message);
                })
                .onItem()
                .transformToUni(m -> Uni.createFrom().completionStage(m.nack(new RuntimeException("Failed to deliver"))))
                .onFailure().recoverWithUni(e -> Uni.createFrom().completionStage(message.nack(e)));
    }

    @Incoming("dead-letter-consumer")
    public Uni<Void> deadLetter(Message<Advisory> message) {
        logger.infof("Received dead letter on dead-letter-consumer channel: %s", message);
        final var advisory = message.getPayload();
        return resolver.<Data> resolve(advisory.getStream(), advisory.getStream_seq())
                .onItem().invoke(dataMessage -> lastData.set(dataMessage.getPayload()))
                .onItem().transformToUni(m -> Uni.createFrom().completionStage(message.ack()))
                .onFailure().recoverWithUni(throwable -> Uni.createFrom().completionStage(message.nack(throwable)));
    }
}
