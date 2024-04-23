package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamOutgoingMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.JetStreamRequestReply;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.RequestReplyConfiguration;
import io.smallrye.mutiny.Uni;

@Path("/request-reply")
@Produces("application/json")
@RequestScoped
public class RequestReplyResource {
    private final JetStreamRequestReply jetStreamRequestReply;

    @Inject
    public RequestReplyResource(JetStreamRequestReply jetStreamRequestReply) {
        this.jetStreamRequestReply = jetStreamRequestReply;
    }

    @GET
    @Path("/streams")
    public Set<String> getLast() {
        return jetStreamRequestReply.getStreams().await().indefinitely();
    }

    @POST
    @Path("/{id}/{data}")
    public Uni<Data> produceData(@PathParam("id") String id, @PathParam("data") String data) {
        final var messageId = UUID.randomUUID().toString();
        final var newMessage = Message.of(new Data(data, id, messageId),
                Metadata.of(new JetStreamOutgoingMessageMetadata(messageId)));
        final var configuration = new RequestReplyConfiguration<Data>() {
            @Override
            public String stream() {
                return "test";
            }

            @Override
            public String subject() {
                return "test-subject";
            }

            @Override
            public Integer replicas() {
                return 1;
            }

            @Override
            public StorageType storageType() {
                return StorageType.Memory;
            }

            @Override
            public RetentionPolicy retentionPolicy() {
                return RetentionPolicy.Interest;
            }

            @Override
            public Class<Data> payloadType() {
                return Data.class;
            }

            @Override
            public boolean traceEnabled() {
                return true;
            }

            @Override
            public Duration pollTimeout() {
                return Duration.ofSeconds(10);
            }

            @Override
            public Optional<Integer> maxDeliver() {
                return Optional.empty();
            }
        };

        return jetStreamRequestReply.request(newMessage, configuration)
                .onItem().transformToUni(reply -> jetStreamRequestReply.nextReply(configuration))
                .onItem().transformToUni(reply -> Uni.createFrom().item(() -> {
                    try {
                        reply.ack().wait();
                        return reply.getPayload();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }));
    }

}
