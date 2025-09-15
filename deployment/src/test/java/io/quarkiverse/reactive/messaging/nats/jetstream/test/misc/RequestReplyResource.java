package io.quarkiverse.reactive.messaging.nats.jetstream.test.misc;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamState;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.RequestReplyConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.JetStreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.MessageConsumer;
import io.smallrye.mutiny.Uni;

@Path("/request-reply")
@Produces("application/json")
@RequestScoped
public class RequestReplyResource implements MessageConsumer<Data> {
    private final ClientFactory clientFactory;
    private final JetStreamConfiguration jetStreamConfiguration;
    private final AtomicReference<Client> messageConnection;

    public RequestReplyResource(ClientFactory clientFactory,
                                JetStreamConfiguration jetStreamConfiguration) {
        this.clientFactory = clientFactory;
        this.jetStreamConfiguration = jetStreamConfiguration;
        this.messageConnection = new AtomicReference<>();
    }

    @GET
    @Path("/streams")
    public Uni<List<String>> getStreams() {
        return getOrEstablishMessageConnection()
                .onItem().transformToUni(Client::streamManagement)
                .onItem().transformToUni(StreamContext::streamNames);
    }

    @GET
    @Path("/streams/{stream}/consumers")
    public Uni<List<String>> getConsumers(@PathParam("stream") String stream) {
        return getOrEstablishMessageConnection()
                .onItem().transformToUni(Client::streamManagement)
                .onItem().transformToUni(streamManagement -> streamManagement.getConsumerNames(stream));
    }

    @GET
    @Path("/streams/{stream}/subjects")
    public Uni<List<String>> getSubjects(@PathParam("stream") String stream) {
        return getOrEstablishMessageConnection()
                .onItem().transformToUni(Client::streamManagement)
                .onItem().transformToUni(streamManagement -> streamManagement.getSubjects(stream));
    }

    @GET
    @Path("/streams/{stream}/state")
    public Uni<StreamState> getStreamState(@PathParam("stream") String stream) {
        return getOrEstablishMessageConnection()
                .onItem().transformToUni(Client::streamManagement)
                .onItem().transformToUni(streamManagement -> streamManagement.getStreamState(stream));
    }

    @POST
    @Path("/request/{id}/{data}")
    public Uni<Data> request(@PathParam("id") String id, @PathParam("data") String data) {
        return getOrEstablishMessageConnection()
                .onItem()
                .<Message<Data>> transformToUni(connection -> connection.request(
                        Message.of(new Data(data, id, UUID.randomUUID().toString())), getRequestReplyConfiguration(id)))
                .onItem().transformToUni(this::acknowledgeData);
    }

    private Uni<Data> acknowledgeData(Message<Data> response) {
        return acknowledge(response)
                .onItem().transform(v -> response.getPayload());
    }

    public void terminate(
            @Observes(notifyObserver = Reception.IF_EXISTS) @Priority(50) @BeforeDestroyed(ApplicationScoped.class) Object ignored) {
        try {
            if (messageConnection.get() != null) {
                messageConnection.get().close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Uni<Client> getOrEstablishMessageConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(messageConnection.get())
                .filter(Client::isConnected)
                .orElse(null))
                .onItem().ifNull()
                .switchTo(() -> clientFactory.create(jetStreamConfiguration.connection()))
                .onItem().invoke(this.messageConnection::set);
    }

    private RequestReplyConsumerConfiguration getRequestReplyConfiguration(String dataId) {
        return new RequestReplyConsumerConfiguration() {

            @Override
            public String name() {
                return dataId;
            }

            @Override
            public String stream() {
                return "request-reply";
            }

            @Override
            public String requestSubject() {
                return "requests";
            }

            @Override
            public ConsumerConfiguration consumerConfiguration() {
                return getConsumerConfiguration(dataId);
            }

            @Override
            public Duration timeout() {
                return Duration.ofSeconds(5);
            }
        };
    }

    private ConsumerConfiguration getConsumerConfiguration(String dataId) {
        return new ConsumerConfiguration() {

            @Override
            public Optional<Long> startSequence() {
                return Optional.empty();
            }

            @Override
            public Optional<ZonedDateTime> pauseUntil() {
                return Optional.empty();
            }

            @Override
            public Optional<Class<?>> payloadType() {
                return Optional.empty();
            }

            @Override
            public DeliverPolicy deliverPolicy() {
                return DeliverPolicy.All;
            }

            @Override
            public Optional<ZonedDateTime> startTime() {
                return Optional.empty();
            }

            @Override
            public Optional<Long> maxAckPending() {
                return Optional.empty();
            }

            @Override
            public Boolean durable() {
                return false;
            }

            @Override
            public List<String> filterSubjects() {
                return List.of("responses." + dataId);
            }

            @Override
            public Optional<Duration> ackWait() {
                return Optional.empty();
            }

            @Override
            public Optional<String> description() {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> inactiveThreshold() {
                return Optional.of(Duration.ofSeconds(30));
            }

            @Override
            public Optional<Long> maxDeliver() {
                return Optional.of(1L);
            }

            @Override
            public ReplayPolicy replayPolicy() {
                return ReplayPolicy.Instant;
            }

            @Override
            public Integer replicas() {
                return 1;
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
            public Optional<List<Duration>> backoff() {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> acknowledgeTimeout() {
                return Optional.of(Duration.ofMillis(1000));
            }
        };
    }
}
