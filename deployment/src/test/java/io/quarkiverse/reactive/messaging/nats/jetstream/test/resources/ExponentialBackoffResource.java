package io.quarkiverse.reactive.messaging.nats.jetstream.test.resources;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.smallrye.mutiny.Uni;

@Path("/exponential-backoff")
@Produces("application/json")
public class ExponentialBackoffResource {

    @Channel("exponential-backoff-producer")
    Emitter<Integer> emitter;

    @Inject
    ExponentialBackoffConsumingBean bean;

    @POST
    @Path("/{data}")
    public Uni<Void> produce(@PathParam("data") Integer data) {
        return Uni.createFrom().item(() -> emitter.send(data)).onItem().ignore().andContinueWithNull();
    }

    @GET
    @Path("/{data}/retries")
    public Uni<Integer> retries(@PathParam("data") Integer data) {
        return Uni.createFrom().item(() -> bean.getNumOfRetries(data));
    }

    @GET
    @Path("/max-delivered")
    public Uni<List<Integer>> maxDelivered() {
        return Uni.createFrom().item(() -> bean.maxDelivered());
    }
}
