package io.quarkiverse.reactive.messaging.nats.jetstream.test.misc;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.DefaultConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.JetStreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.MessageConsumer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class RequestConsumer implements MessageConsumer<Data> {
    private final ConnectionFactory connectionFactory;
    private final JetStreamConfiguration jetStreamConfiguration;
    private final AtomicReference<Connection> connection;

    public RequestConsumer(ConnectionFactory connectionFactory, JetStreamConfiguration jetStreamConfiguration) {
        this.connectionFactory = connectionFactory;
        this.jetStreamConfiguration = jetStreamConfiguration;
        this.connection = new AtomicReference<>();
    }

    @Incoming("request-consumer")
    public Uni<Void> data(final Message<Data> message) {
        return getOrEstablishConnection()
                .onItem().transformToUni(connection -> connection.publish(Message.of(message.getPayload()), "request-reply", "responses." + message.getPayload().resourceId()))
                .onItem().transform(m -> null)
                .onFailure().recoverWithUni(throwable -> notAcknowledge(message, throwable))
                .onItem().transform(v -> null);
    }

    private Uni<Connection> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                        .filter(Connection::isConnected)
                        .orElse(null))
                .onItem().ifNull()
                .switchTo(() -> connectionFactory.create(jetStreamConfiguration.connection(),
                        new DefaultConnectionListener()))
                .onItem().invoke(this.connection::set);
    }
}
