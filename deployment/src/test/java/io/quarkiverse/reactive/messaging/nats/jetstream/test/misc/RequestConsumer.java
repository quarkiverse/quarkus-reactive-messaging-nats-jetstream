package io.quarkiverse.reactive.messaging.nats.jetstream.test.misc;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.MessageConsumer;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class RequestConsumer implements MessageConsumer<Data> {
    private final ClientFactory clientFactory;
    private final ConnectorConfiguration jetStreamConfiguration;
    private final AtomicReference<Client> connection;

    public RequestConsumer(ClientFactory clientFactory, ConnectorConfiguration jetStreamConfiguration) {
        this.clientFactory = clientFactory;
        this.jetStreamConfiguration = jetStreamConfiguration;
        this.connection = new AtomicReference<>();
    }

    @Incoming("request-consumer")
    public Uni<Void> data(final Message<Data> message) {
        return getOrEstablishConnection()
                .onItem()
                .transformToUni(connection -> connection.publish(Message.of(message.getPayload()), "request-reply",
                        "responses." + message.getPayload().resourceId()))
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(throwable -> notAcknowledge(message, throwable))
                .onItem().transform(v -> null);
    }

    private Uni<Client> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Client::isConnected)
                .orElse(null))
                .onItem().ifNull()
                .switchTo(() -> clientFactory.create(jetStreamConfiguration.connection(),
                        new DefaultConnectionListener()))
                .onItem().invoke(this.connection::set);
    }
}
