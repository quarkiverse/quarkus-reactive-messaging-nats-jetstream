package io.quarkiverse.reactive.messaging.nats.client.it;

import java.nio.charset.StandardCharsets;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.smallrye.mutiny.Uni;

/**
 * Reads and writes the {@code test} key-value bucket created on startup from configuration.
 */
@Path("/key-value/{key}")
@ApplicationScoped
public class KeyValueResource {

    @Inject
    Client client;

    @PUT
    @Consumes("text/plain")
    @Produces("text/plain")
    public Uni<Long> put(@PathParam("key") String key, String value) {
        return client.keyValue("test").put(key, value.getBytes(StandardCharsets.UTF_8))
                .map(entry -> entry.revision());
    }

    @GET
    @Produces("text/plain")
    public Uni<String> get(@PathParam("key") String key) {
        return client.keyValue("test").get(key)
                .map(entry -> entry == null ? null : entry.value().map(String::new).orElse(null))
                .onItem().ifNull().failWith(NotFoundException::new);
    }

    @DELETE
    public Uni<Void> delete(@PathParam("key") String key) {
        return client.keyValue("test").delete(key);
    }
}
