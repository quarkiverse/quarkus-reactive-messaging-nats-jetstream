package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import java.util.HashMap;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamOutgoingMessageMetadata;
import io.smallrye.mutiny.Uni;

@Path("/data")
@Produces("application/json")
public class DataResource {

    @Inject
    DataConsumingBean bean;

    @Channel("data")
    Emitter<String> emitter;

    @GET
    @Path("/last")
    public Data getLast() {
        return bean.getLast().orElseGet(Data::new);
    }

    @POST
    @Path("/{id}/{data}")
    public Uni<Void> produceData(@PathParam("id") String id, @PathParam("data") String data) {
        return emitData(id, data)
                .onItem().transformToUni(message -> Uni.createFrom().completionStage(message.ack()));
    }

    private Uni<Message<String>> emitData(String id, String data) {
        return Uni.createFrom().item(() -> {
            final var headers = new HashMap<String, List<String>>();
            headers.put("RESOURCE_ID", List.of(data));
            final var message = Message.of(data, Metadata.of(new JetStreamOutgoingMessageMetadata(id, headers, null)));
            emitter.send(message);
            return message;
        });
    }
}
