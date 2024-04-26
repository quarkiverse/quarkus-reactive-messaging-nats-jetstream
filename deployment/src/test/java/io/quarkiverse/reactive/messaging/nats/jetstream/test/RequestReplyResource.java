package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamOutgoingMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamConsumerType;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.JetStreamUtility;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.RequestReplyConfiguration;

@Path("/request-reply")
@Produces("application/json")
@RequestScoped
public class RequestReplyResource {
    private final RequestReplyConfiguration<Data> configuration;
    private final JetStreamUtility jetStreamUtility;

    @Inject
    public RequestReplyResource(JetStreamUtility jetStreamUtility) {
        this.jetStreamUtility = jetStreamUtility;
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
            public Optional<Integer> replicas() {
                return Optional.of(1);
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
            public Optional<Class<Data>> payloadType() {
                return Optional.of(Data.class);
            }

            @Override
            public boolean traceEnabled() {
                return true;
            }

            @Override
            public Duration connectionTimeout() {
                return Duration.ofSeconds(10);
            }

            @Override
            public Optional<Integer> maxWaiting() {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> maxRequestExpires() {
                return Optional.of(Duration.ofSeconds(20));
            }

            @Override
            public JetStreamConsumerType type() {
                return JetStreamConsumerType.Pull;
            }

            @Override
            public List<String> filterSubjects() {
                return List.of();
            }

            @Override
            public Optional<Duration> ackWait() {
                return Optional.empty();
            }

            @Override
            public Optional<DeliverPolicy> deliverPolicy() {
                return Optional.empty();
            }

            @Override
            public Optional<Long> startSeq() {
                return Optional.empty();
            }

            @Override
            public Optional<ZonedDateTime> startTime() {
                return Optional.empty();
            }

            @Override
            public Optional<String> description() {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> inactiveThreshold() {
                return Optional.empty();
            }

            @Override
            public Optional<Integer> maxAckPending() {
                return Optional.empty();
            }

            @Override
            public Optional<ReplayPolicy> replayPolicy() {
                return Optional.empty();
            }

            @Override
            public Optional<Boolean> memoryStorage() {
                return Optional.empty();
            }

            @Override
            public Optional<String> sampleFrequency() {
                return Optional.empty();
            }

            @Override
            public Map<String, String> metadata() {
                return Map.of();
            }

            @Override
            public List<Duration> backoff() {
                return List.of();
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
        return jetStreamUtility.getStreams(Duration.ofSeconds(1));
    }

    @POST
    @Path("/{id}/{data}")
    public void produceData(@PathParam("id") String id, @PathParam("data") String data) {
        final var messageId = UUID.randomUUID().toString();
        final var newMessage = Message.of(new Data(data, id, messageId),
                Metadata.of(new JetStreamOutgoingMessageMetadata(messageId)));
        jetStreamUtility.publish(newMessage, configuration, Duration.ofSeconds(1));
    }

    @GET
    public Data consumeData() {
        return jetStreamUtility.nextMessage(configuration).map(Message::getPayload)
                .orElse(new Data());
    }
}
