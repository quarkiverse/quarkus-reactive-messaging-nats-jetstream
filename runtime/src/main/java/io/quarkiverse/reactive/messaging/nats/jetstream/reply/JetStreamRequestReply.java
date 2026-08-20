package io.quarkiverse.reactive.messaging.nats.jetstream.reply;

import java.util.Map;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.EmitterType;

/**
 * A reactive request/reply client for NATS JetStream. Inject it on the requestor side with a channel annotation, e.g.:
 *
 * <pre>
 * {@code
 * @Inject
 * @Channel("requests")
 * JetStreamRequestReply<String, JsonPojo> requestor;
 * }
 * </pre>
 *
 * Each call to {@link #request(Object)} publishes the payload on the annotated outgoing channel and returns a {@link Uni}
 * that completes when a matching reply arrives on the configured (or derived) reply subject. Replies are matched to
 * requests using a correlation id stored in the {@value #CORRELATION_ID_HEADER} message header; unmatched replies are
 * ignored and acknowledged, so concurrent requestors never receive each other's replies.
 */
public interface JetStreamRequestReply<Req, Rep> extends EmitterType {

    /** The NATS message header carrying the correlation id of an in-flight request. */
    String CORRELATION_ID_HEADER = "REPLY_CORRELATION_ID";

    /**
     * The NATS message header a requestor uses to advertise the subject on which it expects its reply. Replier beans can
     * simply return their response and the connector routes it there automatically, without explicit metadata.
     */
    String REPLY_SUBJECT_HEADER = "REPLY_SUBJECT";

    /** Suffix appended to the channel subject to derive the default reply subject when none is configured. */
    String DEFAULT_REPLY_SUBJECT_SUFFIX = ".replies";

    long DEFAULT_TIMEOUT_MILLIS = 5_000L;

    /**
     * Default {@code reply.inactive-threshold} in milliseconds: how long NATS keeps an idle reply consumer before reclaiming
     * it.
     */
    long DEFAULT_INACTIVE_THRESHOLD_MILLIS = 60_000L;

    /**
     * Sends a request and waits for the matching reply.
     *
     * @param payload the request payload published on the channel
     * @return a {@link Uni} that completes with the deserialized reply, fails with
     *         {@link JetStreamRequestTimeoutException} if no reply arrives within the configured timeout, or with the
     *         failure returned by the configured {@link ReplyFailureHandler}
     */
    Uni<Rep> request(Req payload);

    /**
     * Sends a fully specified request message and waits for the matching reply.
     *
     * @param <M> the message type
     * @param message the outgoing request message; its metadata may carry an explicit {@code subject} or headers that are
     *        preserved on the published message
     * @return a {@link Uni} that completes with the reply message (payload and NATS metadata) matching this request
     */
    <M extends Message<? extends Req>> Uni<Message<Rep>> request(M message);

    /**
     * @return an unmodifiable view of the correlation ids currently awaiting their reply, useful for observability
     */
    Map<String, PendingReply> getPendingReplies();
}
