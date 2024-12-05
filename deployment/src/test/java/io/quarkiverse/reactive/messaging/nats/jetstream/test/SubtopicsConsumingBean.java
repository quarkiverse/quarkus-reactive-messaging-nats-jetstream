package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamIncomingMessageMetadata;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.annotations.Blocking;

@ApplicationScoped
public class SubtopicsConsumingBean {
    private final static Logger logger = Logger.getLogger(SubtopicsConsumingBean.class);

    volatile Optional<SubjectData> lastData = Optional.empty();

    @Blocking
    @Incoming("subtopics-consumer")
    public Uni<Void> data(Message<String> message) {
        return Uni.createFrom().item(() -> handleData(message))
                .onItem().transformToUni(m -> Uni.createFrom().completionStage(m.ack()))
                .onFailure().recoverWithUni(throwable -> Uni.createFrom().completionStage(message.nack(throwable)));
    }

    public Optional<SubjectData> getLast() {
        return lastData;
    }

    private Message<String> handleData(Message<String> message) {
        logger.infof("Received message: %s", message);
        message.getMetadata(JetStreamIncomingMessageMetadata.class)
                .ifPresent(metadata -> lastData = Optional.of(
                        new SubjectData(message.getPayload(), metadata.headers().get("RESOURCE_ID").get(0),
                                metadata.messageId(),
                                metadata.subject())));
        return message;
    }
}
