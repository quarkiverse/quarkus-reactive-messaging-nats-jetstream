package io.quarkiverse.reactive.messaging.nats.jetstream.test.resources;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Context;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.DefaultConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ExponentialBackoffConsumingBean {
    private final static Logger logger = Logger.getLogger(ExponentialBackoffConsumingBean.class);

    private final AtomicReference<Map<Integer, Integer>> retries;
    private final AtomicReference<List<Integer>> maxDeliveries;
    private final NatsConfiguration natsConfiguration;
    private final ConnectionFactory connectionFactory;
    private final AtomicReference<Connection> connection;
    private final TracerFactory tracerFactory;
    private final Context context;

    public ExponentialBackoffConsumingBean(NatsConfiguration natsConfiguration, ConnectionFactory connectionFactory,
            TracerFactory tracerFactory, Context context) {
        this.natsConfiguration = natsConfiguration;
        this.connectionFactory = connectionFactory;
        this.connection = new AtomicReference<>();
        this.retries = new AtomicReference<>(new HashMap<>());
        this.tracerFactory = tracerFactory;
        this.context = context;
        this.maxDeliveries = new AtomicReference<>(new ArrayList<>());
    }

    public int getNumOfRetries(int d) {
        return retries.get().getOrDefault(d, 0);
    }

    public List<Integer> maxDelivered() {
        return maxDeliveries.get();
    }

    @Incoming("exponential-backoff-consumer")
    public Uni<Void> exponentialBackoffConsumer(Message<Integer> message) {
        return Uni.createFrom().item(message)
                .onItem().invoke(() -> {
                    logger.infof("Received message on exponential-backoff-consumer: %s", message);
                    int r = retries.get().getOrDefault(message.getPayload(), 0);
                    retries.get().put(message.getPayload(), ++r);
                    message.nack(new RuntimeException("Failed to deliver"));
                })
                .onFailure().invoke(message::nack)
                .onItem().ignore().andContinueWithNull();
    }

    @Incoming("max-deliveries-consumer")
    public Uni<Void> maxDeliveries(Message<Advisory> message) {
        logger.infof("Received messge on max-deliveries-consumer channel: %s", message);
        return getOrEstablishConnection().onItem().transformToUni(connection -> maxDeliveries(connection, message));
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

    private Uni<Void> maxDeliveries(Connection connection, Message<Advisory> message) {
        final var advisory = message.getPayload();
        return context
                .withContext(ctx -> connection.<Integer> resolve(advisory.stream(), advisory.stream_seq(),
                        tracerFactory.create(), ctx))
                .onItem().invoke(msg -> {
                    maxDeliveries.get().add(msg.getPayload());
                    message.ack();
                })
                .onFailure().invoke(message::nack)
                .replaceWithVoid();
    }

    private Uni<Connection> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Connection::isConnected)
                .orElse(null))
                .onItem().ifNull().switchTo(() -> connectionFactory
                        .create(ConnectionConfiguration.of(natsConfiguration), new DefaultConnectionListener()))
                .onItem().invoke(this.connection::set);
    }
}
