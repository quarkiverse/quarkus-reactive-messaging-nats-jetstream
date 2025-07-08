package io.quarkiverse.reactive.messaging.nats.jetstream.test.misc;

import java.util.HashMap;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;
import io.smallrye.mutiny.Uni;

@Path("/subjects")
@Produces("application/json")
public class SubjectDataResource {

    @Inject
    SubjectDataConsumingBean bean;

    @Channel("data")
    Emitter<String> emitter;

    @GET
    @Path("/last")
    public SubjectData getLast() {
        return bean.getLast().orElseGet(() -> new SubjectData("", "", "", ""));
    }

    @POST
    @Path("/{subtopic}/{id}/{data}")
    public Uni<Void> produceData(@PathParam("subtopic") String subtopic, @PathParam("id") String id,
            @PathParam("data") String data) {
        return Uni.createFrom().item(() -> emitData(subtopic, id, data))
                .onItem().ignore().andContinueWithNull();
    }

    private Message<String> emitData(String subtopic, String id, String data) {
        final var headers = new HashMap<String, List<String>>();
        headers.put("RESOURCE_ID", List.of(data));
        final var message = Message.of(data, Metadata
                .of(PublishMessageMetadata.builder().messageId(id).subject("data." + subtopic).headers(headers).build()));
        emitter.send(message);
        return message;
    }
}
