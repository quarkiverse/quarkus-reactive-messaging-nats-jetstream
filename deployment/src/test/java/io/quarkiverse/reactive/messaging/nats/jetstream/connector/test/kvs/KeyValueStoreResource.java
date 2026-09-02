package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.kvs;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.KeyValueEntry;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientRegistry;
import io.smallrye.mutiny.Uni;

@Path("/key-value")
@Produces("application/json")
@RequestScoped
class KeyValueStoreResource {
    private final Client client;
    private final Serializer serializer;

    @Inject
    public KeyValueStoreResource(ClientRegistry clientRegistry, Serializer serializer) {
        this.client = clientRegistry.lookup(ClientRegistry.DEFAULT_CLIENT_NAME);
        this.serializer = serializer;
    }

    @GET
    @Path("{key}")
    public Uni<Data> getValue(
            @PathParam("key") String key) {
        return client.keyValue("test").get(key)
                .onItem().ifNotNull().transform(value -> serializer.readValue(value.value()
                        .orElseThrow(() -> new IllegalArgumentException("Key data not found")), Data.class));
    }

    @PUT
    @Path("{key}")
    @Consumes("application/json")
    public Uni<Long> putValue(@PathParam("key") String key,
            Data data) {
        return client.keyValue("test").put(key, serializer.toBytes(data))
                .map(KeyValueEntry::revision);
    }

    @DELETE
    @Path("{key}")
    @Consumes("application/json")
    public Uni<Void> deleteValue(@PathParam("key") String key) {
        return client.keyValue("test").delete(key);
    }
}
