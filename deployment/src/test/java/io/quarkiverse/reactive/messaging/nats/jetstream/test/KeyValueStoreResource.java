package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.smallrye.mutiny.Uni;

@Path("/key-value")
@Produces("application/json")
@RequestScoped
public class KeyValueStoreResource {
    private final ConnectionFactory connectionFactory;
    private final NatsConfiguration natsConfiguration;
    private final AtomicReference<Connection> connection;

    @Inject
    public KeyValueStoreResource(ConnectionFactory connectionFactory, NatsConfiguration natsConfiguration) {
        this.connectionFactory = connectionFactory;
        this.natsConfiguration = natsConfiguration;
        this.connection = new AtomicReference<>();
    }

    @GET
    @Path("{key}")
    public Uni<Data> getValue(@PathParam("key") String key) {
        return getOrEstablishConnection().onItem().transformToUni(keyValueConnection -> getValue(keyValueConnection, key));
    }

    @PUT
    @Path("{key}")
    @Consumes("application/json")
    public Uni<Void> putValue(@PathParam("key") String key, Data data) {
        return getOrEstablishConnection().onItem()
                .transformToUni(keyValueConnection -> putValue(keyValueConnection, key, data));
    }

    @DELETE
    @Path("{key}")
    @Consumes("application/json")
    public Uni<Void> deleteValue(@PathParam("key") String key) {
        return getOrEstablishConnection().onItem().transformToUni(connection -> deleteValue(connection, key));
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

    private Uni<Data> getValue(Connection connection, String key) {
        return connection.getKeyValue("test", key, Data.class)
                .onItem().ifNull().failWith(new NotFoundException())
                .onFailure().transform(failure -> new NotFoundException(failure.getMessage()));
    }

    public Uni<Void> putValue(Connection keyValueConnection, String key, Data data) {
        return keyValueConnection.putKeyValue("test", key, data);
    }

    public Uni<Void> deleteValue(Connection connection, String key) {
        return connection.deleteKeyValue("test", key);
    }

    private Uni<Connection> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Connection::isConnected)
                .orElse(null))
                .onItem().ifNull()
                .switchTo(() -> connectionFactory.create(ConnectionConfiguration.of(natsConfiguration), (event, message) -> {
                }))
                .onItem().invoke(this.connection::set);
    }
}
