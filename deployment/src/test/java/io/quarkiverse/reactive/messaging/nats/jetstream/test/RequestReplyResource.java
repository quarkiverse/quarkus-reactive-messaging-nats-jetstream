package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamOutgoingMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.JetStreamStreamUtility;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.RequestReplyConfiguration;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/request-reply")
@Produces("application/json")
@RequestScoped
public class RequestReplyResource {

    @GET
    @Path("/streams")
    public List<String> getLast() {
        final var utility = new JetStreamStreamUtility();
        return utility.getStreams(Duration.ofSeconds(1));
    }

    @POST
    @Path("/{id}/{data}")
    public Data produceData(@PathParam("id") String id, @PathParam("data") String data) {
        final var messageId = UUID.randomUUID().toString();
        final var newMessage = Message.of(new Data(data, id, messageId),
                Metadata.of(new JetStreamOutgoingMessageMetadata(messageId)));
        final var configuration = new RequestReplyConfiguration<Data>() {
            @Override
            public String stream() {
                return "request-reply";
            }

            @Override
            public String subject() {
                return "test";
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
                return Duration.ofSeconds(20);
            }

            @Override
            public Optional<Integer> maxDeliver() {
                return Optional.empty();
            }

            @Override
            public Optional<String> durable() {
                return Optional.of("test-durable");
            }
        };
        final var utility = new JetStreamStreamUtility();
        utility.publish(newMessage, configuration, Duration.ofSeconds(1));
        return utility.pullNextMessage(configuration, Duration.ofSeconds(1), Duration.ofSeconds(10)).map(Message::getPayload).orElse(null);
    }

}
