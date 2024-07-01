package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamOutgoingMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.JetStreamPublishConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.JetStreamUtility;

@Path("/request-reply")
@Produces("application/json")
@RequestScoped
public class RequestReplyResource {
    private final JetStreamUtility jetStreamUtility;
    private final String streamName;

    @Inject
    public RequestReplyResource(JetStreamUtility jetStreamUtility) {
        this.jetStreamUtility = jetStreamUtility;
        this.streamName = "request-reply";
    }

    @GET
    @Path("/streams")
    public List<String> getStreams() {
        try (JetStreamClient client = jetStreamUtility.getJetStreamClient()) {
            try (Connection connection = jetStreamUtility.getConnection(client, Duration.ofSeconds(1))) {
                return jetStreamUtility.getStreams(connection);
            }
        }
    }

    @GET
    @Path("/streams/{stream}/consumers")
    public List<String> getConsumers(@PathParam("stream") String stream) {
        try (JetStreamClient client = jetStreamUtility.getJetStreamClient()) {
            try (Connection connection = jetStreamUtility.getConnection(client, Duration.ofSeconds(1))) {
                return jetStreamUtility.getConsumerNames(connection, stream);
            }
        }
    }

    @GET
    @Path("/streams/{stream}/subjects")
    public List<String> getSubjects(@PathParam("stream") String stream) {
        try (JetStreamClient client = jetStreamUtility.getJetStreamClient()) {
            try (Connection connection = jetStreamUtility.getConnection(client, Duration.ofSeconds(1))) {
                return jetStreamUtility.getSubjects(connection, stream);
            }
        }
    }

    @POST
    @Path("/subjects/{subject}/{id}/{data}")
    public void produceData(@PathParam("subject") String subject, @PathParam("id") String id, @PathParam("data") String data) {
        try (JetStreamClient client = jetStreamUtility.getJetStreamClient()) {
            try (Connection connection = jetStreamUtility.getConnection(client, Duration.ofSeconds(1))) {
                final var messageId = UUID.randomUUID().toString();
                final var newMessage = Message.of(new Data(data, id, messageId),
                        Metadata.of(new JetStreamOutgoingMessageMetadata(messageId)));
                jetStreamUtility.addOrUpdateConsumer(connection, getConsumerConfiguration(streamName, subject));
                jetStreamUtility.publish(connection, newMessage, new JetStreamPublishConfiguration() {
                    @Override
                    public boolean traceEnabled() {
                        return true;
                    }

                    @Override
                    public String stream() {
                        return streamName;
                    }

                    @Override
                    public String subject() {
                        return "events." + subject;
                    }
                });
            }
        }
    }

    @GET
    @Path("/subjects/{subject}")
    public Data consumeData(@PathParam("subject") String subject) {
        try (JetStreamClient client = jetStreamUtility.getJetStreamClient()) {
            try (Connection connection = jetStreamUtility.getConnection(client, Duration.ofSeconds(1))) {
                return jetStreamUtility.nextMessage(connection, getConsumerConfiguration(streamName, subject))
                        .map(message -> {
                            message.ack();
                            return message.getPayload();
                        })
                        .orElseThrow(NotFoundException::new);
            }
        }
    }

    private ConsumerConfiguration<Data> getConsumerConfiguration(String streamName, String subject) {
        return new ConsumerConfiguration() {
            @Override
            public String stream() {
                return streamName;
            }

            @Override
            public String subject() {
                return "events." + subject;
            }

            @Override
            public String name() {
                return subject;
            }

            @Override
            public boolean traceEnabled() {
                return true;
            }

            @Override
            public Optional<Duration> ackTimeout() {
                return Optional.empty();
            }

            @Override
            public Optional<Class> getPayloadType() {
                return Optional.empty();
            }
        };
    }
}
