package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.subscriber;

import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.Stream;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ChannelConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.PublisherChannelConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.Health;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessageProcessor;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.helpers.MultiUtils;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class MessageSubscriberProcessor<T> implements MessageProcessor {
    private final PublisherChannelConfiguration channelConfiguration;

    private final AtomicReference<Health> health;
    private final AtomicReference<Cancellable> verification;
    private final Client client;

    private volatile boolean stopped;
    private volatile boolean verified;

    public MessageSubscriberProcessor(@NonNull final PublisherChannelConfiguration channelConfiguration,
            @NonNull Client client) {
        this.channelConfiguration = channelConfiguration;
        this.client = client;
        this.health = new AtomicReference<>(new Health(false, "Subscriber processor inactive"));
        this.verification = new AtomicReference<>();
        this.stopped = false;
        this.verified = false;
    }

    public Flow.Subscriber<Message<T>> subscriber() {
        return MultiUtils.via(this::subscribe);
    }

    private Multi<Message<T>> subscribe(Multi<Message<T>> subscription) {
        // Failures are handled per message: a failed publish must not terminate the stream, because the upstream
        // processor cannot be re-subscribed once cancelled and would silently drop every subsequent message.
        return subscription.onSubscription().invoke(this::verify)
                .onItem().transformToUniAndMerge(message -> publish(message)
                        .onItem().invoke(() -> {
                            // Until the stream and subject are verified, a successful publish must not report the channel ready
                            if (verified) {
                                health.set(new Health(true,
                                        "Subscriber processor active for channel: " + channelConfiguration.name()));
                            }
                        })
                        .onFailure().invoke(throwable -> {
                            log.errorf(throwable, "Failed to publish message on channel: %s", channelConfiguration.name());
                            health.set(new Health(false,
                                    "Subscriber processor error for channel: " + channelConfiguration.name()
                                            + " with message: " + throwable.getMessage()));
                        })
                        .onFailure().recoverWithNull())
                .onTermination().invoke(this::cancelVerification);
    }

    @Override
    public @NonNull ChannelConfiguration channelConfiguration() {
        return channelConfiguration;
    }

    @Override
    public @NonNull Health health() {
        return health.get();
    }

    @Override
    public void stop() {
        this.stopped = true;
        cancelVerification();
    }

    /**
     * Reports the processor ready once the stream and subject of the channel are verified, retrying until they are.
     * This only drives the health status, so messages are published (and fail) exactly as without it. Replaces any
     * verification still in progress.
     */
    private void verify() {
        final var cancellable = verifyStreamAndSubject()
                .onFailure().invoke(failure -> health.set(new Health(false,
                        "Subscriber processor not ready for channel: " + channelConfiguration.name() + " with message: "
                                + failure.getMessage())))
                .onFailure().retry().withBackOff(channelConfiguration.getRetryBackoff()).until(failure -> !stopped)
                .subscribe().with(
                        stream -> {
                            verified = true;
                            health.set(new Health(true,
                                    "Subscriber processor ready for channel: " + channelConfiguration.name()));
                        },
                        failure -> log.debugf(failure, "Stopped verifying channel: %s", channelConfiguration.name()));
        final var previous = verification.getAndSet(cancellable);
        if (previous != null) {
            previous.cancel();
        }
    }

    private void cancelVerification() {
        final var cancellable = verification.getAndSet(null);
        if (cancellable != null) {
            cancellable.cancel();
        }
    }

    /**
     * Verifies that the stream of the channel exists and that one of its subjects matches the subject of the
     * channel, so the channel is not reported ready before messages published to it can be stored.
     */
    private Uni<Stream> verifyStreamAndSubject() {
        return client.streamManagement().stream(channelConfiguration.stream())
                .onItem().ifNull().failWith(() -> new IllegalStateException(
                        String.format("Stream %s not found", channelConfiguration.stream())))
                .onItem().invoke(Unchecked.consumer(stream -> {
                    final var subjects = stream.configuration().subjects();
                    if (subjects.stream().noneMatch(pattern -> Subjects.matches(pattern, channelConfiguration.subject()))) {
                        throw new IllegalStateException(String.format("Subject %s not found on stream %s with subjects %s",
                                channelConfiguration.subject(), channelConfiguration.stream(), subjects));
                    }
                }));
    }

    private Uni<Message<T>> publish(Message<T> message) {
        return client.publish(message, channelConfiguration.stream(), channelConfiguration.subject());
    }
}
