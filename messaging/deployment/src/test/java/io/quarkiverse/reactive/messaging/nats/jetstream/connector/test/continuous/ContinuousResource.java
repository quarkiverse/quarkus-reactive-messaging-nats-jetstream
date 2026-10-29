package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.continuous;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.smallrye.mutiny.Uni;

@Path("/continuous")
@Produces("application/json")
public class ContinuousResource {

    @Channel("continuous-producer")
    Emitter<Integer> producer;

    @Channel("continuous-typed-producer")
    Emitter<Integer> typedProducer;

    @Inject
    ContinuousConsumingBean bean;

    @POST
    @Path("/values/{value}")
    public Uni<Void> produce(@PathParam("value") Integer value) {
        return Uni.createFrom().completionStage(() -> producer.send(value));
    }

    @POST
    @Path("/typed-values/{value}")
    public Uni<Void> produceTyped(@PathParam("value") Integer value) {
        return Uni.createFrom().completionStage(() -> typedProducer.send(value));
    }

    @GET
    @Path("/values")
    public Uni<List<Integer>> values() {
        return Uni.createFrom().item(bean.getValues());
    }

    @GET
    @Path("/typed-values")
    public Uni<List<Integer>> typedValues() {
        return Uni.createFrom().item(bean.getTypedValues());
    }
}
