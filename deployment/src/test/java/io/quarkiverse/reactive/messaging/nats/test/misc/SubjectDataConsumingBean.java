package io.quarkiverse.reactive.messaging.nats.test.misc;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.client.api.SubscribeMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.test.MessageConsumer;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class SubjectDataConsumingBean implements MessageConsumer<String> {
    private final static Logger logger = Logger.getLogger(SubjectDataConsumingBean.class);

    volatile Optional<SubjectData> lastData = Optional.empty();

    @Incoming("data-consumer")
    public Uni<Void> data(Message<String> message) {
        return handleData(message)
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(throwable -> notAcknowledge(message, throwable));
    }

    public Optional<SubjectData> getLast() {
        return lastData;
    }

    private Uni<Message<String>> handleData(Message<String> message) {
        return Uni.createFrom().item(() -> message)
                .onItem().invoke(m -> logger.infof("Received message: %s", m))
                .onItem().transform(m -> {
                    m.getMetadata(SubscribeMessageMetadata.class)
                            .ifPresent(metadata -> lastData = Optional.of(
                                    new SubjectData(message.getPayload(), metadata.headers().get("RESOURCE_ID").get(0),
                                            metadata.messageId(),
                                            metadata.subject())));
                    return m;
                });
    }
}
