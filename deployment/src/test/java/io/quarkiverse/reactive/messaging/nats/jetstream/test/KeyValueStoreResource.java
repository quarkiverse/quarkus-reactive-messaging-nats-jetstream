package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.JetStreamUtility;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.KeyValueStore;
import io.smallrye.mutiny.Uni;

@Path("/key-value")
@Produces("application/json")
@RequestScoped
public class KeyValueStoreResource {
    private final JetStreamUtility jetStreamUtility;
    private final KeyValueStore keyValueStore;

    @Inject
    public KeyValueStoreResource(JetStreamUtility jetStreamUtility, KeyValueStore keyValueStore) {
        this.jetStreamUtility = jetStreamUtility;
        this.keyValueStore = keyValueStore;
    }

    @GET
    @Path("{key}")
    public Uni<Data> getValue(@PathParam("key") String key) {
        JetStreamClient client = jetStreamUtility.getJetStreamClient();
        return client.getOrEstablishConnection()
                .onItem().transformToUni(connection -> keyValueStore.get(connection, "test", key, Data.class))
                .onItem().invoke(client::close)
                .onItem().ifNull().failWith(new NotFoundException());
    }

    @PUT
    @Path("{key}")
    @Consumes("application/json")
    public Uni<Void> putValue(@PathParam("key") String key, Data data) {
        JetStreamClient client = jetStreamUtility.getJetStreamClient();
        return client.getOrEstablishConnection()
                .onItem().transformToUni(connection -> keyValueStore.put(connection, "test", key, data))
                .onItem().invoke(client::close);
    }

    @DELETE
    @Path("{key}")
    @Consumes("application/json")
    public Uni<Void> deleteValue(@PathParam("key") String key) {
        JetStreamClient client = jetStreamUtility.getJetStreamClient();
        return client.getOrEstablishConnection()
                .onItem().transformToUni(connection -> keyValueStore.delete(connection, "test", key))
                .onItem().invoke(client::close);
    }

}
