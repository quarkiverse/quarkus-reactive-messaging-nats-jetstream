package io.quarkiverse.reactive.messaging.nats.jetstream.test.tracing;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.test.MessageConsumer;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class DataCollectorBean
        implements MessageConsumer<Data> {
    private final static Logger logger = Logger
            .getLogger(DataCollectorBean.class);

    private final AtomicReference<Data> lastData = new AtomicReference<>();

    @Incoming("data-collector")
    public Uni<Void> data(Message<Data> message) {
        return Uni.createFrom().item(message)
                .onItem().invoke(m -> logger.infof("Received message: %s", message))
                .onItem().transformToUni(this::setLast)
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(throwable -> notAcknowledge(message, throwable));
    }

    private Uni<Message<Data>> setLast(
            Message<Data> message) {
        return Uni.createFrom().item(() -> {
            lastData.set(message.getPayload());
            return message;
        });
    }

    public Optional<Data> getLast() {
        return Optional.ofNullable(lastData.get());
    }

}
