package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamOutgoingMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.JetStreamStreamUtility;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.RequestReplyConfiguration;

@Path("/request-reply")
@Produces("application/json")
@RequestScoped
public class RequestReplyResource {
    private final RequestReplyConfiguration<Data> configuration;

    public RequestReplyResource() {
        this.configuration = new RequestReplyConfiguration<>() {
            @Override
            public String stream() {
                return "request-reply";
            }

            @Override
            public String subject() {
                return "requests";
            }

            @Override
            public Integer replicas() {
                return 1;
            }

            @Override
            public StorageType storageType() {
                return StorageType.File;
            }

            @Override
            public RetentionPolicy retentionPolicy() {
                return RetentionPolicy.WorkQueue;
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
                return Optional.of(1);
            }

            @Override
            public Optional<String> durable() {
                return Optional.of("test-request-reply");
            }
        };
    }

    @GET
    @Path("/streams")
    public List<String> getStream() {
        final var utility = new JetStreamStreamUtility();
        return utility.getStreams(Duration.ofSeconds(1));
    }

    @POST
    @Path("/{id}/{data}")
    public void produceData(@PathParam("id") String id, @PathParam("data") String data) {
        final var messageId = UUID.randomUUID().toString();
        final var newMessage = Message.of(new Data(data, id, messageId),
                Metadata.of(new JetStreamOutgoingMessageMetadata(messageId)));
        final var utility = new JetStreamStreamUtility();
        utility.publish(newMessage, configuration, Duration.ofSeconds(1));
    }

    @GET
    public Data consumeData() {
        final var utility = new JetStreamStreamUtility();
        return utility.pullNextMessage(configuration, Duration.ofSeconds(1), Duration.ofSeconds(10)).map(Message::getPayload)
                .orElse(new Data());
    }
}
