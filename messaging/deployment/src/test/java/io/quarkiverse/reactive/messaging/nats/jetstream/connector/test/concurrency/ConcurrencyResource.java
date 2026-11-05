package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.concurrency;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.smallrye.mutiny.Uni;

@Path("/concurrency")
@Produces("application/json")
public class ConcurrencyResource {

    @Channel("concurrency-producer")
    Emitter<Integer> producer;

    @Inject
    ConcurrencyConsumingBean bean;

    @POST
    @Path("/values/{value}")
    public Uni<Void> produce(@PathParam("value") Integer value) {
        return Uni.createFrom().completionStage(() -> producer.send(value));
    }

    @GET
    @Path("/values")
    public Uni<List<Integer>> values() {
        return Uni.createFrom().item(bean.getValues());
    }
}
