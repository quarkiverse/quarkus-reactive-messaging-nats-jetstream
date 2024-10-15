package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.DefaultConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class DeadLetterConsumingBean {
    private final static Logger logger = Logger.getLogger(DeadLetterConsumingBean.class);

    private final AtomicReference<Data> lastData;
    private final AtomicReference<Connection> connection;
    private final NatsConfiguration natsConfiguration;
    private final ConnectionFactory connectionFactory;

    @Inject
    public DeadLetterConsumingBean(NatsConfiguration natsConfiguration, ConnectionFactory connectionFactory) {
        this.connection = new AtomicReference<>();
        this.natsConfiguration = natsConfiguration;
        this.connectionFactory = connectionFactory;
        this.lastData = new AtomicReference<>();
    }

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
        return getOrEstablishConnection().onItem().transformToUni(connection -> deadLetter(connection, message));
    }

    public void terminate(
            @Observes(notifyObserver = Reception.IF_EXISTS) @Priority(50) @BeforeDestroyed(ApplicationScoped.class) Object ignored) {
        try {
            if (connection.get() != null) {
                connection.get().close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Uni<Void> deadLetter(Connection connection, Message<Advisory> message) {
        logger.infof("Received dead letter on dead-letter-consumer channel: %s", message);
        final var advisory = message.getPayload();
        return connection.<Data> resolve(advisory.stream(), advisory.stream_seq())
                .onItem().invoke(dataMessage -> lastData.set(dataMessage.getPayload()))
                .onItem().transformToUni(m -> Uni.createFrom().completionStage(message.ack()))
                .onFailure().recoverWithUni(throwable -> Uni.createFrom().completionStage(message.nack(throwable)));
    }

    private Uni<Connection> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Connection::isConnected)
                .orElse(null))
                .onItem().ifNull()
                .switchTo(() -> connectionFactory.create(ConnectionConfiguration.of(natsConfiguration),
                        new DefaultConnectionListener()))
                .onItem().invoke(this.connection::set);
    }
}
