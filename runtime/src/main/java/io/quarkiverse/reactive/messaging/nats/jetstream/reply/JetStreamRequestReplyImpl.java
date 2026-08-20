package io.quarkiverse.reactive.messaging.nats.jetstream.reply;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnector;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfigurationImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PushConfigurationImpl;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.reactive.messaging.EmitterConfiguration;
import io.smallrye.reactive.messaging.providers.extension.MutinyEmitterImpl;
import io.smallrye.reactive.messaging.providers.helpers.CDIUtils;
import io.smallrye.reactive.messaging.providers.impl.Configs;
import lombok.extern.jbosslog.JBossLog;

/**
 * Default {@link JetStreamRequestReply} implementation. Requests are published with the standard SmallRye emitter path on
 * the annotated channel; a lazily created ephemeral push consumer receives replies, matches them by correlation id and
 * completes or fails the outstanding call. Unmatched replies (including traffic from other requestor instances) are
 * acknowledged and dropped. If the reply subscription fails it is transparently re-created on the next request.
 */
@JBossLog
public class JetStreamRequestReplyImpl<Req, Rep> extends MutinyEmitterImpl<Req> implements JetStreamRequestReply<Req, Rep> {

    private final Client client;
    private final String channelName;
    private final String stream;
    private final String replySubject;
    private final long timeoutMillis;
    private final Duration inactiveThreshold;
    private final CorrelationIdHandler correlationIdHandler;
    private final ReplyFailureHandler failureHandler;

    private final Map<String, PendingReply> pendingReplies = new ConcurrentHashMap<>();

    private final Object initLock = new Object();
    private volatile CompletableFuture<Void> readyFuture;
    private volatile boolean active;
    private volatile boolean closed;
    private volatile Cancellable replyCancellable;
    private volatile String replyConsumerName;

    public JetStreamRequestReplyImpl(final EmitterConfiguration config,
            final Client client,
            final Config mpConfig,
            final Instance<CorrelationIdHandler> correlationIdHandlers,
            final Instance<ReplyFailureHandler> failureHandlers) {
        super(config, 1024L);
        requireNonNull(client, "client");
        this.client = client;
        this.channelName = config.name();

        final var connectorConfig = Configs.outgoing(mpConfig, JetStreamConnector.CONNECTOR_NAME, config.name());
        this.stream = requiredConfig(connectorConfig, "stream", channelName);
        this.replySubject = nonBlank(connectorConfig.getOptionalValue("reply.subject", String.class))
                .orElse(requiredConfig(connectorConfig, "subject", channelName)
                        + JetStreamRequestReply.DEFAULT_REPLY_SUBJECT_SUFFIX);
        this.timeoutMillis = connectorConfig.getOptionalValue("reply.timeout", Long.class)
                .orElse(JetStreamRequestReply.DEFAULT_TIMEOUT_MILLIS);
        if (this.timeoutMillis <= 0) {
            throw new IllegalArgumentException(
                    "reply.timeout must be a positive number of milliseconds for channel '" + channelName + "'");
        }

        final var inactiveThresholdMillis = connectorConfig.getOptionalValue("reply.inactive-threshold", Long.class)
                .orElse(JetStreamRequestReply.DEFAULT_INACTIVE_THRESHOLD_MILLIS);
        if (inactiveThresholdMillis < 0) {
            throw new IllegalArgumentException(
                    "reply.inactive-threshold must not be negative for channel '" + channelName + "'");
        }
        this.inactiveThreshold = Duration.ofMillis(inactiveThresholdMillis);

        final var correlationIdHandlerId = nonBlank(
                connectorConfig.getOptionalValue("reply.correlation-id.handler", String.class))
                .orElse(UuidCorrelationIdHandler.ID);
        this.correlationIdHandler = resolveHandler(correlationIdHandlers, CorrelationIdHandler.class,
                channelName, "reply.correlation-id.handler", correlationIdHandlerId);

        final var failureHandlerId = nonBlank(connectorConfig.getOptionalValue("reply.failure.handler", String.class));
        if (failureHandlerId.isPresent()) {
            this.failureHandler = resolveHandler(failureHandlers, ReplyFailureHandler.class,
                    channelName, "reply.failure.handler", failureHandlerId.get());
        } else {
            this.failureHandler = null;
        }
    }

    @Override
    public Uni<Rep> request(final Req payload) {
        final Message<Req> message = Message.of(payload);
        return request(message).onItem().transform(reply -> reply.getPayload());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <M extends Message<? extends Req>> Uni<Message<Rep>> request(final M message) {
        final var correlationId = correlationIdHandler.generate();
        final var pending = new PendingReply(correlationId, Instant.now());
        pendingReplies.put(correlationId, pending);

        return Uni.createFrom().completionStage(ensureReady())
                .chain(() -> publishAndAwaitReply(message, correlationId, pending))
                .ifNoItem()
                .after(Duration.ofMillis(timeoutMillis))
                .failWith(() -> new JetStreamRequestTimeoutException(correlationId, timeoutMillis))
                .onTermination().invoke(() -> pendingReplies.remove(correlationId));
    }

    @SuppressWarnings("unchecked")
    private Uni<Message<Rep>> publishAndAwaitReply(final Message<? extends Req> message, final String correlationId,
            final PendingReply pending) {
        // The emitter's send-Uni only completes once a downstream acknowledges the message; on a plain outgoing channel
        // nobody does that, so we must not gate the reply future on it. Subscribe fire-and-forget and propagate any
        // publication failure to the pending reply instead of waiting for the ack.
        final var reply = Uni.createFrom().completionStage(pending.future())
                .onItem().transform(m -> (Message<Rep>) m);
        sendMessage(withCorrelationId(message, correlationId))
                .subscribe()
                .with(unused -> {
                    // published (and acknowledged, if ever) - the reply future drives completion
                }, failure -> pending.fail(new JetStreamRequestPublishException(channelName, failure)));
        return reply;
    }

    @Override
    public Map<String, PendingReply> getPendingReplies() {
        return Map.copyOf(pendingReplies);
    }

    /**
     * Shuts this emitter down: cancels the reply subscription, deletes the ephemeral consumer (best effort) and fails all
     * outstanding requests with a {@link JetStreamRequestShutdownException}. Further calls to {@link #request(Object)} fail
     * immediately. Idempotent; invoked by the factory on application shutdown.
     */
    public void close() {
        synchronized (initLock) {
            if (closed) {
                return;
            }
            closed = true;
        }
        resetSubscription();

        final var consumerName = replyConsumerName;
        if (consumerName != null) {
            client.deleteConsumer(stream, consumerName).subscribe().with(unused -> {
                // deleted
            }, failure -> log.debugf(failure, "Could not delete JetStream request/reply consumer '%s'", consumerName));
        }

        for (final var pending : pendingReplies.values()) {
            if (!pending.isDone()) {
                pending.fail(new JetStreamRequestShutdownException(channelName));
            }
        }
    }

    private Uni<Void> publish(final Message<? extends Req> message, final String correlationId) {
        return sendMessage(withCorrelationId(message, correlationId))
                .onFailure().transform(failure -> new JetStreamRequestPublishException(channelName, failure));
    }

    /**
     * Returns a future that completes once the reply consumer is created and subscribed. Initialization is single-flight:
     * concurrent callers share one attempt, and a failed attempt is retried on the next call. Never blocks, so it is safe
     * to invoke from any thread (including Vert.x event loops).
     */
    private CompletableFuture<Void> ensureReady() {
        if (closed) {
            final var failed = new CompletableFuture<Void>();
            failed.completeExceptionally(new JetStreamRequestShutdownException(channelName));
            return failed;
        }
        if (active) {
            return CompletableFuture.completedFuture(null);
        }
        synchronized (initLock) {
            if (closed) {
                final var failed = new CompletableFuture<Void>();
                failed.completeExceptionally(new JetStreamRequestShutdownException(channelName));
                return failed;
            }
            if (active) {
                return CompletableFuture.completedFuture(null);
            }
            final var current = readyFuture;
            if (current != null && !current.isDone()) {
                return current;
            }
            final var future = new CompletableFuture<Void>();
            readyFuture = future;
            startReplyConsumer(future);
            return future;
        }
    }

    private void startReplyConsumer(final CompletableFuture<Void> future) {
        final var consumerName = "rr-reply-" + channelName + "-" + UUID.randomUUID();
        replyConsumerName = consumerName;
        // A placeholder deliver subject makes the consumer push-based on the server; the client rebinds it to its own
        // inbox when subscribing. It must not be covered by any stream so nothing is delivered there.
        final var deliverSubject = "_js_reply." + consumerName;

        final var consumerConfiguration = ConsumerConfigurationImpl.builder()
                .name(consumerName)
                .stream(stream)
                .durable(false)
                .filterSubjects(List.of(replySubject))
                .ackWait(Optional.empty())
                .deliverPolicy(DeliverPolicy.New)
                .startSequence(Optional.empty())
                .startTime(Optional.empty())
                .description(Optional.empty())
                .inactiveThreshold(Optional.of(inactiveThreshold))
                .maxAckPending(Optional.empty())
                .maxDeliver(Optional.of(1L))
                .replayPolicy(ReplayPolicy.Instant)
                .replicas(Optional.empty())
                .memoryStorage(Optional.empty())
                .sampleFrequency(Optional.empty())
                .metadata(Map.of())
                .backoff(Optional.empty())
                .pauseUntil(Optional.empty())
                .acknowledgeTimeout(Duration.ofSeconds(1))
                .build();

        final var pushConfiguration = PushConfigurationImpl.builder()
                .ordered(false)
                .deliverSubject(deliverSubject)
                .flowControl(Optional.empty())
                .idleHeartbeat(Optional.of(Duration.ofMillis(750)))
                .rateLimit(Optional.empty())
                .headersOnly(Optional.empty())
                .deliverGroup(Optional.empty())
                .build();
        client.addConsumerIfAbsent(consumerConfiguration, pushConfiguration)
                .chain(() -> {
                    replyCancellable = client.subscribe(consumerConfiguration, pushConfiguration, newReplyListener())
                            .subscribe().with(unused -> {
                                // replies are handled by the listener; items reaching here have already been dispatched
                            }, this::onSubscriptionFailure);
                    active = true;
                    return Uni.createFrom().voidItem();
                })
                .subscribe()
                .with(unused -> future.complete(null), failure -> {
                    onInitFailure(failure);
                    future.completeExceptionally(new JetStreamRequestSubscriptionException(channelName, replySubject, failure));
                });
    }

    private ConsumerListener<Rep> newReplyListener() {
        return new ConsumerListener<>() {
            @Override
            public void onMessage(Message<Rep> message) {
                onReply(message);
            }

            @Override
            public void onError(Throwable throwable) {
                onSubscriptionFailure(throwable);
            }
        };
    }

    private void onInitFailure(final Throwable failure) {
        log.errorf(failure,
                "JetStream request/reply consumer for channel '%s' could not be started; it will retry on the next request",
                channelName);
        resetSubscription();
    }

    private synchronized void onSubscriptionFailure(final Throwable failure) {
        log.warnf(failure,
                "JetStream request/reply reply consumer for channel '%s' failed; it will restart on the next request",
                channelName);
        resetSubscription();
    }

    private void resetSubscription() {
        active = false;
        final var cancellable = replyCancellable;
        if (cancellable != null) {
            try {
                cancellable.cancel();
            } catch (RuntimeException ignored) {
                // best effort only
            }
        }
        replyCancellable = null;
    }

    private void onReply(final Message<?> message) {
        final Optional<String> correlationId = firstHeader(message, JetStreamRequestReply.CORRELATION_ID_HEADER)
                .flatMap(correlationIdHandler::parse);
        try {
            if (correlationId.isEmpty() || !pendingReplies.containsKey(correlationId.get())) {
                log.debugf("Dropping unmatched reply with subject '%s'", firstHeader(message, "subject").orElse("?"));
                return;
            }
            final var pending = pendingReplies.get(correlationId.get());
            if (pending != null && !pending.isDone()) {
                final Object payload = message.getPayload();
                final Optional<Throwable> businessFailure = failureHandler == null ? Optional.empty()
                        : failureHandler.failure(payload);
                if (businessFailure.isPresent()) {
                    pending.fail(businessFailure.get());
                } else {
                    @SuppressWarnings("unchecked")
                    final Message<Rep> typedReply = (Message<Rep>) message;
                    pending.complete(typedReply);
                }
            }
        } finally {
            ackQuietly(message);
        }
    }

    private <M extends Message<? extends Req>> M withCorrelationId(final M message, final String correlationId) {
        final var existing = message.getMetadata().get(PublishMessageMetadata.class);
        final var headers = new HashMap<>(existing.map(PublishMessageMetadata::headers).orElseGet(Map::of));
        headers.put(JetStreamRequestReply.CORRELATION_ID_HEADER, List.of(correlationId));
        headers.put(JetStreamRequestReply.REPLY_SUBJECT_HEADER, List.of(replySubject));
        final var metadata = existing.map(pm -> new PublishMessageMetadata(
                pm.stream(),
                pm.subject(),
                pm.messageId(),
                pm.payload(),
                headers,
                pm.sequence())).orElseGet(() -> new PublishMessageMetadata(
                        stream,
                        null,
                        null,
                        null,
                        headers,
                        null));
        final var newMetadata = message.getMetadata().without(PublishMessageMetadata.class).with(metadata);
        return (M) message.withMetadata(newMetadata);
    }

    private static Optional<String> firstHeader(final Message<?> message, final String headerName) {
        return Optional.ofNullable(message)
                .map(Message::getMetadata)
                .flatMap(metadata -> metadata.get(SubscribeMessageMetadata.class))
                .map(subscribeMetadata -> subscribeMetadata.headers().get(headerName))
                .filter(values -> !values.isEmpty())
                .map(values -> values.get(0));
    }

    private static void ackQuietly(final Message<?> message) {
        try {
            final var future = message.ack();
            if (future != null) {
                // non-blocking: the reply listener runs on a Vert.x event loop and must not be parked here
                future.toCompletableFuture().whenComplete((unused, failure) -> {
                    if (failure != null) {
                        log.debugf(failure, "Failed to acknowledge reply message");
                    }
                });
            }
        } catch (RuntimeException e) {
            log.debugf(e, "Failed to start acknowledging reply message");
        }
    }

    private static <T> T resolveHandler(final Instance<T> candidates, final Class<T> type, final String channelName,
            final String attribute, final String id) {
        try {
            return CDIUtils.getInstanceById(candidates, id).get();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "No " + type.getSimpleName() + " with id '" + id + "' found for channel '" + channelName
                            + "'. Define a CDI bean annotated with @Identifier(\"" + id + "\") or set '" + attribute
                            + "' to an available handler id.",
                    e);
        }
    }

    private static Optional<String> nonBlank(final Optional<String> value) {
        return value.map(String::trim).filter(s -> !s.isEmpty());
    }

    private static String requiredConfig(final Config mpConfig, final String key, final String channelName) {
        try {
            return requireNonNull(mpConfig.getOptionalValue(key, String.class).orElse(null),
                    "channel '" + channelName + "' requires a '" + key + "' connector attribute");
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("channel '" + channelName + "' requires a '" + key + "' connector attribute", e);
        }
    }

}
