package io.quarkiverse.reactive.messaging.nats.jetstream.test.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.smallrye.mutiny.Uni;

@Path("/redelivery")
@Produces("application/json")
public class RedeliveryResource {

    @Channel("unstable-producer")
    Emitter<Integer> unstableEmitter;

    @Inject
    RedeliveryConsumingBean bean;

    @POST
    @Path("{data}")
    public Uni<Void> produce(@PathParam("data") Integer data) {
        return Uni.createFrom().item(() -> unstableEmitter.send(data)).onItem().ignore().andContinueWithNull();
    }

    @GET
    @Path("last")
    public Uni<Integer> last() {
        return Uni.createFrom().item(() -> bean.getLast());
    }

}
