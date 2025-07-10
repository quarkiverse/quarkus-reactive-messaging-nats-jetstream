package io.quarkiverse.reactive.messaging.nats.jetstream.test.subscription;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.smallrye.mutiny.Uni;

@Path("/durable")
@Produces("application/json")
public class DurableResource {

    @Channel("durable-consumer")
    Emitter<Integer> emitter;

    @Inject
    DurableConsumingBean bean;

    @POST
    @Path("/{data}")
    public Uni<Void> produce(@PathParam("data") Integer data) {
        return Uni.createFrom().item(() -> emitter.send(data)).onItem().ignore().andContinueWithNull();
    }

    @GET
    @Path("values")
    public Uni<List<Integer>> values() {
        return Uni.createFrom().item(() -> bean.getValues());
    }

}
