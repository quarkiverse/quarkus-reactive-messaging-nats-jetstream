package io.quarkiverse.reactive.messaging.nats.jetstream.test.reply;

import static io.restassured.RestAssured.*;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.parsing.Parser;

class ReplyTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(ReplierBean.class, FailOnFailHandler.class, FixedCorrelationIdHandler.class,
                            ReplyResource.class))
            .withConfigurationResource("application-reply.properties");

    @BeforeEach
    void setup() {
        defaultParser = Parser.JSON;
    }

    @Test
    void roundTrip() {
        given().filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .pathParam("value", "hello")
                .post("/rr/a/{value}")
                .then()
                .statusCode(200)
                .body(equalTo("echo:hello"));

        given().get("/rr/pending").then().statusCode(200).body("a", equalTo(0));
    }

    @Test
    void twoRequestorsOnlyReceiveOwnReplies() {
        // Both requestors share the reply subject; each must complete with only its own correlation id.
        given().pathParam("value", "one").post("/rr/a/{value}").then().statusCode(200).body(equalTo("echo:one"));
        given().pathParam("value", "two").post("/rr/b/{value}").then().statusCode(200).body(equalTo("echo:two"));

        given().get("/rr/pending").then().statusCode(200)
                .body("a", equalTo(0), "b", equalTo(0));
    }

    @Test
    void timeoutFailsAndLateReplyIsDropped() {
        // Reply subject must be covered so the reply consumer can be created; nobody consumes 'slow'.
        given().pathParam("subject", "slow.replies").post("/rr/add-subject/{subject}").then().statusCode(200);

        final var response = given().filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .pathParam("value", "slow-1")
                .post("/rr/slow/{value}")
                .then()
                .statusCode(500)
                .extract();

        final Map<String, Object> body = response.as(Map.class);
        assertEquals("JetStreamRequestTimeoutException", body.get("exception"));
        final var correlationId = (String) body.get("correlationId");
        assertNotNull(correlationId);
        assertFalse(correlationId.isBlank());

        // Pending entry removed on timeout (guards the leak fix).
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> (Integer) get("/rr/pending").as(Map.class).get("slow") == 0);

        // A late reply for the timed-out correlation id must be discarded without error.
        given().pathParam("id", correlationId).post("/rr/late/{id}").then().statusCode(200);
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> (Integer) get("/rr/pending").as(Map.class).get("slow") == 0);

        // The requestor is still usable afterwards.
        given().pathParam("value", "slow-2").post("/rr/slow/{value}").then().statusCode(500)
                .body("exception", equalTo("JetStreamRequestTimeoutException"));
    }

    @Test
    void publishFailurePropagates() {
        // Reply subject covered, request subject not: consumer creation succeeds, the publish must fail.
        given().pathParam("subject", "bad.replies").post("/rr/add-subject/{subject}").then().statusCode(200);

        given().filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .post("/rr/bad")
                .then()
                .statusCode(500)
                .body("exception", equalTo("JetStreamRequestPublishException"));
    }

    @Test
    void succeedsOnceReplySubjectIsCovered() {
        // Reply subject not covered yet: the request is published but no reply can ever arrive, so it times out.
        given().filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .post("/rr/fail")
                .then()
                .statusCode(500)
                .body("exception", equalTo("JetStreamRequestTimeoutException"));

        // Extend stream coverage, then the next request must succeed.
        given().pathParam("subject", "fail.replies").post("/rr/add-subject/{subject}").then().statusCode(200);

        given().filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .post("/rr/fail")
                .then()
                .statusCode(200)
                .body(equalTo("echo:hello-fail"));
    }

    @Test
    void reSubscribesAfterInitialSubscriptionFailure() throws Exception {
        // Stream does not exist yet: the first request must fail with a subscription error.
        given().filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .post("/rr/missing")
                .then()
                .statusCode(500)
                .body("exception", equalTo("JetStreamRequestSubscriptionException"));

        // Create the stream, then the next request must re-subscribe and complete once a reply arrives.
        given().post("/rr/add-stream-missing").then().statusCode(200);

        final var result = new java.util.concurrent.CompletableFuture<String>();
        final var thread = new Thread(() -> {
            try {
                final var response = given().post("/rr/missing").then().extract();
                if (response.statusCode() == 200) {
                    result.complete(response.body().asString());
                } else {
                    result.completeExceptionally(new AssertionError("expected 200 but was " + response.statusCode() + ": "
                            + response.asString()));
                }
            } catch (RuntimeException e) {
                result.completeExceptionally(e);
            }
        }, "request-missing");
        thread.start();

        // The request is in flight (re-subscribed and waiting for a reply): publish the matching reply out-of-band.
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> Integer.valueOf(1).equals(get("/rr/pending").as(Map.class).get("missing")));
        given().pathParam("id", FixedCorrelationIdHandler.ID).post("/rr/reply-missing/{id}").then().statusCode(200);

        assertEquals("echo:hello-missing", result.get(5, TimeUnit.SECONDS));
    }

    @Test
    void failureHandlerPropagatesAndPendingIsCleared() {
        given().filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .pathParam("value", "please FAIL now")
                .post("/rr/fh/{value}")
                .then()
                .statusCode(500)
                .body("exception", equalTo("IllegalStateException"));

        given().get("/rr/pending").then().statusCode(200).body("fh", equalTo(0));
    }

}
