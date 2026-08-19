package io.quarkiverse.reactive.messaging.nats.jetstream.test.reply;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.reply.JetStreamRequestReply;
import io.quarkiverse.reactive.messaging.nats.jetstream.reply.JetStreamRequestTimeoutException;

@ApplicationScoped
@Path("/rr")
public class ReplyResource {

    private static final Duration BOUND = Duration.ofSeconds(10);

    @Inject
    @Channel("requests")
    JetStreamRequestReply<String, String> requestorA;

    @Inject
    @Channel("requests-2")
    JetStreamRequestReply<String, String> requestorB;

    @Inject
    @Channel("requests-slow")
    JetStreamRequestReply<String, String> requestorSlow;

    @Inject
    @Channel("requests-bad")
    JetStreamRequestReply<String, String> requestorBad;

    @Inject
    @Channel("requests-fail")
    JetStreamRequestReply<String, String> requestorFail;

    @Inject
    @Channel("requests-fh")
    JetStreamRequestReply<String, String> requestorFh;

    @Inject
    @Channel("requests-missing")
    JetStreamRequestReply<String, String> requestorMissing;

    @Inject
    Client client;

    @POST
    @Path("/a/{value}")
    public Object a(@PathParam("value") final String value) {
        return invoke(requestorA, value);
    }

    @POST
    @Path("/b/{value}")
    public Object b(@PathParam("value") final String value) {
        return invoke(requestorB, value);
    }

    @POST
    @Path("/slow/{value}")
    public Object slow(@PathParam("value") final String value) {
        return invoke(requestorSlow, value);
    }

    @POST
    @Path("/bad")
    public Object bad() {
        return invoke(requestorBad, "x");
    }

    @POST
    @Path("/fail")
    public Object fail() {
        return invoke(requestorFail, "hello-fail");
    }

    @POST
    @Path("/missing")
    public Object missing() {
        return invoke(requestorMissing, "hello-missing");
    }

    @POST
    @Path("/fh/{value}")
    public Object fh(@PathParam("value") final String value) {
        return invoke(requestorFh, value);
    }

    @GET
    @Path("/pending")
    public Map<String, Integer> pending() {
        final var map = new LinkedHashMap<String, Integer>();
        map.put("a", requestorA.getPendingReplies().size());
        map.put("b", requestorB.getPendingReplies().size());
        map.put("slow", requestorSlow.getPendingReplies().size());
        map.put("fh", requestorFh.getPendingReplies().size());
        map.put("missing", requestorMissing.getPendingReplies().size());
        return map;
    }

    @POST
    @Path("/add-subject/{subject}")
    public Response addSubject(@PathParam("subject") final String subject) {
        try {
            client.addSubject("rr", subject).await().atMost(BOUND);
        } catch (RuntimeException e) {
            // Subject already present - fine.
        }
        return Response.ok().build();
    }

    @POST
    @Path("/add-stream-missing")
    public Response addStreamMissing() {
        final var configuration = io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfigurationImpl
                .builder()
                .name("missing")
                .description(java.util.Optional.empty())
                .subjects(java.util.Set.of("missing-req", "missing.replies"))
                .replicas(1)
                .storageType(io.nats.client.api.StorageType.File)
                .retentionPolicy(io.nats.client.api.RetentionPolicy.Limits)
                .compressionOption(io.nats.client.api.CompressionOption.None)
                .maximumConsumers(java.util.Optional.empty())
                .maximumMessages(java.util.Optional.empty())
                .maximumMessagesPerSubject(java.util.Optional.empty())
                .maximumBytes(java.util.Optional.empty())
                .maximumAge(java.util.Optional.empty())
                .maximumMessageSize(java.util.Optional.empty())
                .templateOwner(java.util.Optional.empty())
                .discardPolicy(java.util.Optional.of(io.nats.client.api.DiscardPolicy.Old))
                .duplicateWindow(java.util.Optional.empty())
                .allowRollup(java.util.Optional.empty())
                .allowDirect(java.util.Optional.empty())
                .mirrorDirect(java.util.Optional.empty())
                .denyDelete(java.util.Optional.empty())
                .denyPurge(java.util.Optional.empty())
                .discardNewPerSubject(java.util.Optional.empty())
                .firstSequence(java.util.Optional.empty())
                .build();
        client.addStreamIfAbsent(configuration).await().atMost(BOUND);
        return Response.ok().build();
    }

    @POST
    @Path("/reply-missing/{id}")
    public Response replyMissing(@PathParam("id") final String id) {
        final var metadata = PublishMessageMetadata.builder()
                .stream("missing")
                .subject("missing.replies")
                .headers(Map.of(JetStreamRequestReply.CORRELATION_ID_HEADER, List.of(id)))
                .build();
        client.publish(Message.of("echo:hello-missing").addMetadata(metadata), "missing", "missing.replies")
                .await().atMost(BOUND);
        return Response.ok().build();
    }

    @POST
    @Path("/late/{id}")
    public Response late(@PathParam("id") final String id) {
        final var metadata = PublishMessageMetadata.builder()
                .stream("rr")
                .subject("slow.replies")
                .headers(Map.of(JetStreamRequestReply.CORRELATION_ID_HEADER, List.of(id)))
                .build();
        client.publish(Message.of("late-reply").addMetadata(metadata), "rr", "slow.replies")
                .await().atMost(BOUND);
        return Response.ok().build();
    }

    private Object invoke(final JetStreamRequestReply<String, String> requestor, final String value) {
        try {
            return requestor.request(value).await().atMost(BOUND);
        } catch (RuntimeException e) {
            final var body = new LinkedHashMap<String, Object>();
            body.put("exception", e.getClass().getSimpleName());
            body.put("message", String.valueOf(e.getMessage()));
            if (e instanceof JetStreamRequestTimeoutException timeout) {
                body.put("correlationId", timeout.getCorrelationId());
            }
            return Response.serverError().entity(body).build();
        }
    }

}
