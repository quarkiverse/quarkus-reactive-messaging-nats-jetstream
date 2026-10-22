package io.quarkiverse.reactive.messaging.nats.client.it;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.AcknowledgeMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.smallrye.mutiny.Uni;

/**
 * Publishes to and consumes from the {@code test} stream through the {@link Client}, without Reactive Messaging.
 */
@Path("/data")
@ApplicationScoped
public class DataResource {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Inject
    Client client;

    @POST
    @Consumes("application/json")
    @Produces("text/plain")
    public Uni<Long> publish(Data data) {
        return client.publish(Message.of(data), "test", "data")
                .map(message -> message.getMetadata(AcknowledgeMetadata.class).orElseThrow().sequenceNumber());
    }

    @GET
    @Path("/next")
    @Produces("application/json")
    public Uni<Data> next() {
        return client.next("test", "data-consumer", TIMEOUT, Data.class)
                .onItem().ifNull().failWith(NotFoundException::new)
                .onItem().transformToUni(message -> Uni.createFrom().completionStage(message.ack())
                        .replaceWith(message.getPayload()));
    }
}
