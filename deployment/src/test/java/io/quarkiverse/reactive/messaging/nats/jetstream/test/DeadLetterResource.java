package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.smallrye.mutiny.Uni;

@Path("/dead-letter")
@Produces("application/json")
public class DeadLetterResource {

    @Channel("unstable-data")
    Emitter<Data> dataEmitter;

    @Inject
    DeadLetterConsumingBean bean;

    @POST
    @Path("/data")
    @Consumes("application/json")
    public Uni<Void> produce(Data data) {
        return Uni.createFrom().item(() -> dataEmitter.send(data)).onItem().ignore().andContinueWithNull();
    }

    @GET
    @Path("last")
    public Data getLast() {
        return bean.getLast().orElseGet(() -> new Data(null, null, null));
    }

}
