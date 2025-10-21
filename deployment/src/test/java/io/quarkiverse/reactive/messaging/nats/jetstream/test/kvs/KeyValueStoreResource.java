package io.quarkiverse.reactive.messaging.nats.jetstream.test.kvs;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.smallrye.mutiny.Uni;

@Path("/key-value")
@Produces("application/json")
@RequestScoped
class KeyValueStoreResource {
    private final Client client;

    @Inject
    public KeyValueStoreResource(Client client) {
        this.client = client;
    }

    @GET
    @Path("{key}")
    public Uni<Data> getValue(
            @PathParam("key") String key) {
        return client.getValue("test", key, Data.class);
    }

    @PUT
    @Path("{key}")
    @Consumes("application/json")
    public Uni<Long> putValue(@PathParam("key") String key,
            Data data) {
        return client.putValue("test", key, data);
    }

    @DELETE
    @Path("{key}")
    @Consumes("application/json")
    public Uni<Void> deleteValue(@PathParam("key") String key) {
        return client.deleteValue("test", key);
    }
}
