package io.quarkiverse.reactive.messaging.nats.jetstream.test.kvs;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;
import io.smallrye.mutiny.Uni;

@Path("/key-value")
@Produces("application/json")
@RequestScoped
class KeyValueStoreResource {
    private final ClientFactory clientFactory;
    private final ConnectorConfiguration jetStreamConfiguration;
    private final AtomicReference<Client> connection;

    @Inject
    public KeyValueStoreResource(ClientFactory clientFactory, ConnectorConfiguration jetStreamConfiguration) {
        this.clientFactory = clientFactory;
        this.jetStreamConfiguration = jetStreamConfiguration;
        this.connection = new AtomicReference<>();
    }

    @GET
    @Path("{key}")
    public Uni<Data> getValue(
            @PathParam("key") String key) {
        return getOrEstablishConnection().onItem().transformToUni(keyValueConnection -> getValue(keyValueConnection, key));
    }

    @PUT
    @Path("{key}")
    @Consumes("application/json")
    public Uni<Void> putValue(@PathParam("key") String key,
            Data data) {
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

    private Uni<Data> getValue(
            Client client, String key) {
        return client.keyValueStore("test")
                .onItem()
                .transformToUni(keyValueStore -> keyValueStore.get(key,
                        Data.class))
                .onItem().ifNull().failWith(new NotFoundException())
                .onFailure().transform(failure -> new NotFoundException(failure.getMessage()));
    }

    public Uni<Void> putValue(Client client,
                              String key, Data data) {
        return client.keyValueStore("test")
                .onItem().transformToUni(keyValueStore -> keyValueStore.put(key, data));
    }

    public Uni<Void> deleteValue(
            Client client, String key) {
        return client.keyValueStore("test")
                .onItem().transformToUni(keyValueStore -> keyValueStore.delete(key));
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
