package io.quarkiverse.reactive.messsaging.nats.jetstream.test;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messsaging.nats.jetstream.administration.MessageResolver;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class DeadLetterConsumingBean {
    private final static Logger logger = Logger.getLogger(DeadLetterConsumingBean.class);

    volatile Optional<Data> lastData = Optional.empty();

    @Inject
    MessageResolver resolver;

    public Optional<Data> getLast() {
        return lastData;
    }

    @Incoming("unstable-data-consumer")
    public Uni<Void> durableConsumer(Message<Data> message) {
        return Uni.createFrom().item(message)
                .onItem().invoke(() -> {
                    logger.infof("Received message on unstable-data-consumer channel: %s", message);
                    message.nack(new RuntimeException("Failed to deliver"));
                })
                .onFailure().invoke(message::nack)
                .onItem().ignore().andContinueWithNull();
    }

    @Incoming("dead-letter-consumer")
    public Uni<Void> deadLetter(Message<Advisory> message) {
        logger.infof("Received dead letter on dead-letter-consumer channel: %s", message);
        final var advisory = message.getPayload();
        return resolver.<Data> resolve(advisory.getStream(), advisory.getStream_seq())
                .onItem().invoke(deadLetter -> {
                    lastData = Optional.of(deadLetter.getPayload());
                    message.ack();
                })
                .onFailure().invoke(message::nack)
                .replaceWithVoid();
    }
}
