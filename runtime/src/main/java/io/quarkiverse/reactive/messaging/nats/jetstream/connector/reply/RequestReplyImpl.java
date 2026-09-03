package io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.PublishException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageHeaders;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.PublishHeaders;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientRegistry;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.PublisherChannelConfiguration;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.EmitterConfiguration;
import io.smallrye.reactive.messaging.providers.extension.MutinyEmitterImpl;
import lombok.extern.jbosslog.JBossLog;

/**
 * Default {@link RequestReply} implementation. Requests are published with the standard SmallRye emitter path on
 * the annotated channel; a lazily created ephemeral push consumer receives replies, matches them by correlation id and
 * completes or fails the outstanding call. Unmatched replies (including traffic from other requestor instances) are
 * acknowledged and dropped. If the reply subscription fails, it is transparently re-created on the next request.
 */
@JBossLog
public class RequestReplyImpl<Req, Rep> extends MutinyEmitterImpl<Req> implements RequestReply<Req, Rep> {
    private final ClientRegistry clientRegistry;
    private final PublisherChannelConfiguration channelConfiguration;
    private final Map<String, PendingReply<Rep>> pendingReplies;

    private final AtomicReference<Consumer> consumerReference = new AtomicReference<>();
    private final AtomicReference<Cancellable> subscriptionReference = new AtomicReference<>();

    public RequestReplyImpl(final EmitterConfiguration config,
            final ClientRegistry clientRegistry,
            final PublisherChannelConfiguration channelConfiguration) {
        super(config, 1024L);
        this.clientRegistry = clientRegistry;
        this.channelConfiguration = channelConfiguration;
        this.pendingReplies = new ConcurrentHashMap<>();
    }

    @Override
    public Uni<Rep> request(final Req payload) {
        final Message<Req> message = Message.of(payload);
        return request(message).onItem().transform(Message::getPayload);
    }

    @Override
    public Map<String, PendingReply<Rep>> getPendingReplies() {
        return pendingReplies;
    }

    @Override
    public <M extends Message<? extends Req>> Uni<Message<Rep>> request(final M message) {
        final var timeout = timeout();
        final var pendingReply = PendingReply.<Rep> builder()
                .correlationId(channelConfiguration.replyCorrelationIdHandler().generate())
                .createdAt(Instant.now()).future(new CompletableFuture<>()).build();
        pendingReplies.put(pendingReply.correlationId(), pendingReply);
        return subscribe()
                .chain(subscription -> {
                    final var publish = sendMessage(withCorrelationId(message, pendingReply.correlationId()))
                            .onFailure()
                            .transform(failure -> new PublishException(
                                    String.format("Failed to publish request on channel '%s'", channelConfiguration.name()),
                                    failure));
                    final var reply = Uni.createFrom().completionStage(pendingReply.future());
                    return Uni.combine().all().unis(publish, reply).asTuple().map(Tuple2::getItem2);
                })
                .ifNoItem().after(timeout)
                .failWith(() -> new TimeoutException(pendingReply.correlationId(), timeout().toMillis()))
                .onTermination().invoke(() -> pendingReplies.remove(pendingReply.correlationId()));
    }

    @SuppressWarnings("unchecked")
    private <M extends Message<? extends Req>> M withCorrelationId(final M message, final String correlationId) {
        final var headers = message.getMetadata(PublishHeaders.class).orElseGet(PublishHeaders::of);
        headers.setCorrelationId(correlationId);
        headers.setReplySubject(replySubject());
        final var newMetadata = message.getMetadata().with(headers);
        return (M) message.withMetadata(newMetadata);
    }

    private String replySubject() {
        return channelConfiguration.replySubject()
                .orElseGet(() -> channelConfiguration.subject() + RequestReply.DEFAULT_REPLY_SUBJECT_SUFFIX);
    }

    private Duration timeout() {
        final var timeout = channelConfiguration.replyTimeout().orElse(DEFAULT_TIMEOUT);
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException(
                    "reply.timeout must be a positive number of milliseconds for channel '" + channelConfiguration.name()
                            + "'");
        }
        return timeout;
    }

    private Duration inactiveThreshold() {
        final var inactiveThreshold = channelConfiguration.replyInactiveThreshold().orElse(DEFAULT_INACTIVE_THRESHOLD);
        if (inactiveThreshold.isNegative()) {
            throw new IllegalArgumentException(
                    "reply.inactive-threshold must not be negative for channel '" + channelConfiguration.name() + "'");
        }
        return inactiveThreshold;
    }

    private void onReply(final Message<Rep> message) {
        try {
            final Optional<String> correlationId = message.getMetadata(MessageHeaders.class)
                    .flatMap(MessageHeaders::correlationId)
                    .flatMap(id -> channelConfiguration.replyCorrelationIdHandler().parse(id));
            if (correlationId.isEmpty() || !pendingReplies.containsKey(correlationId.get())) {
                log.debugf("Dropping unmatched reply with subject '%s'",
                        message.getMetadata(MessageHeaders.class).flatMap(MessageHeaders::subject).orElse("?"));
                return;
            }
            Optional.ofNullable(pendingReplies.get(correlationId.get())).filter(pending -> !pending.done())
                    .ifPresent(pending -> {
                        final Rep payload = message.getPayload();
                        channelConfiguration.replyFailureHandler()
                                .flatMap(failureHandler -> failureHandler.failure(payload))
                                .ifPresentOrElse(pending::fail, () -> pending.complete(message));
                    });
        } finally {
            ackQuietly(message);
        }
    }

    private void onSubscriptionFailure(final Throwable failure) {
        log.warnf(failure,
                "JetStream request/reply reply consumer for channel '%s' failed; it will restart on the next request",
                channelConfiguration.name());
        reset();
    }

    private Uni<Cancellable> subscribe() {
        return Uni.createFrom().item(Unchecked.supplier(() -> clientRegistry.lookup(channelConfiguration.datasource())))
                .chain(client -> getConsumer(client).map(consumer -> Tuple2.of(client, consumer)))
                .chain(tuple -> getSubscription(tuple.getItem1(), tuple.getItem2()))
                .onFailure().invoke(this::reset);
    }

    private Uni<Cancellable> getSubscription(Client client, Consumer consumer) {
        return Uni.createFrom().item(Unchecked.supplier(() -> subscriptionReference.updateAndGet(subscription -> {
            if (subscription == null) {
                return startSubscription(client, consumer).await().indefinitely();
            }
            return subscription;
        })));
    }

    private Uni<Cancellable> startSubscription(Client client, Consumer consumer) {
        return Uni.createFrom()
                .item(Unchecked.supplier(() -> client.<Rep> subscribe(channelConfiguration.stream(), consumer.name())
                        .subscribe().with(this::onReply, this::onSubscriptionFailure)));
    }

    private Uni<Consumer> getConsumer(Client client) {
        return Uni.createFrom().item(Unchecked.supplier(() -> consumerReference.updateAndGet(consumer -> {
            if (consumer == null) {
                return addConsumer(client).await().indefinitely();
            }
            return consumer;
        })));
    }

    private Uni<Consumer> addConsumer(Client client) {
        final var consumerName = "rr-reply-" + channelConfiguration.name() + "-" + UUID.randomUUID();
        final var deliverSubject = "_js_reply." + consumerName;
        final var configuration = new ConsumerConfiguration(consumerName, replySubject(), deliverSubject,
                Optional.of(inactiveThreshold()));
        return client.consumerManagement(channelConfiguration.stream()).addIfAbsent(configuration);
    }

    @SuppressWarnings("resource")
    void reset() {
        subscriptionReference.updateAndGet(subscription -> {
            if (subscription != null) {
                try {
                    subscription.cancel();
                } catch (RuntimeException ignored) {
                    // best effort only
                }
            }
            return null;
        });
        consumerReference.updateAndGet(consumer -> {
            if (consumer != null) {
                final var client = clientRegistry.lookup(channelConfiguration.datasource());
                client.consumerManagement(channelConfiguration.stream()).delete(consumer.name());
            }
            return null;
        });
    }

    private void ackQuietly(final Message<Rep> message) {
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
}
