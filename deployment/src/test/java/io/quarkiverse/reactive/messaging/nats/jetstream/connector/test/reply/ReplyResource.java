package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.reply;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Channel;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.PublishHeaders;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientRegistry;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.RequestReply;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.TimeoutException;

@SuppressWarnings("resource")
@ApplicationScoped
@Path("/rr")
public class ReplyResource {

    private static final Duration BOUND = Duration.ofSeconds(10);

    @Inject
    @Channel("requests")
    RequestReply<String, String> requestorA;

    @Inject
    @Channel("requests-2")
    RequestReply<String, String> requestorB;

    @Inject
    @Channel("requests-slow")
    RequestReply<String, String> requestorSlow;

    @Inject
    @Channel("requests-bad")
    RequestReply<String, String> requestorBad;

    @Inject
    @Channel("requests-fail")
    RequestReply<String, String> requestorFail;

    @Inject
    @Channel("requests-fh")
    RequestReply<String, String> requestorFh;

    @Inject
    @Channel("requests-missing")
    RequestReply<String, String> requestorMissing;

    @Inject
    ClientRegistry clientRegistry;

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
        final var stream = clientRegistry.lookup(ClientRegistry.DEFAULT_CLIENT_NAME).streamManagement()
                .addSubject("rr", subject).await()
                .atMost(BOUND);
        return Response.ok(stream).build();
    }

    @POST
    @Path("/add-stream-missing")
    public Response addStreamMissing() {
        final var configuration = new StreamConfiguration();
        clientRegistry.lookup(ClientRegistry.DEFAULT_CLIENT_NAME).streamManagement().addIfAbsent(configuration).await()
                .atMost(BOUND);
        return Response.ok().build();
    }

    @POST
    @Path("/reply-missing/{id}")
    public Response replyMissing(@PathParam("id") final String id) {
        final var headers = PublishHeaders.of();
        headers.setCorrelationId(id);
        headers.setPayloadType(String.class);
        clientRegistry.lookup(ClientRegistry.DEFAULT_CLIENT_NAME)
                .publish(Message.of("echo:hello-missing".getBytes(StandardCharsets.UTF_8), headers), "missing",
                        "missing.replies")
                .await().atMost(BOUND);
        return Response.ok().build();
    }

    @POST
    @Path("/late/{id}")
    public Response late(@PathParam("id") final String id) {
        final var headers = PublishHeaders.of();
        headers.setCorrelationId(id);
        headers.setPayloadType(String.class);
        clientRegistry.lookup(ClientRegistry.DEFAULT_CLIENT_NAME)
                .publish(Message.of("late-reply".getBytes(StandardCharsets.UTF_8), headers), "rr", "slow.replies")
                .await().atMost(BOUND);
        return Response.ok().build();
    }

    private Object invoke(final RequestReply<String, String> requestor, final String value) {
        try {
            return requestor.request(value).await().atMost(BOUND);
        } catch (RuntimeException e) {
            final var body = new LinkedHashMap<String, Object>();
            body.put("exception", e.getClass().getSimpleName());
            body.put("message", String.valueOf(e.getMessage()));
            if (e instanceof TimeoutException timeout) {
                body.put("correlationId", timeout.getCorrelationId());
            }
            return Response.serverError().entity(body).build();
        }
    }

}
