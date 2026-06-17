package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.jspecify.annotations.NonNull;

import io.nats.client.api.AckPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.ConsumerInfo;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class VertxStreamManagement implements StreamManagement {
    private final ClientConfiguration configuration;
    private final Connection connection;
    private final Context context;
    private final Consumer consumer;

    @Override
    public @NonNull Uni<ConsumerInfo> addConsumerIfAbsent(@NonNull final String stream, @NonNull final ConsumerConfiguration configuration) {
        return this.consumer.consumer(stream, configuration.name())
                .onItem().ifNull().switchTo(() -> createConsumer(stream, configuration));
    }

    @Override
    public @NonNull Uni<Void> deleteConsumer(@NonNull final String stream, @NonNull final String consumer) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.deleteConsumer(stream, consumer))))
                .chain(deleted -> deleted ? Uni.createFrom().voidItem()
                        : Uni.createFrom()
                        .failure(() -> new RuntimeException(
                                String.format("Consumer %s in stream %s not deleted", consumer, stream))))
                .runSubscriptionOn(this.configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Void> pauseConsumer(@NonNull final String stream, @NonNull final String consumer,
                                            @NonNull final ZonedDateTime pauseUntil) {
        return jetStreamManagement().chain(jetStreamManagement -> Uni.createFrom().item(
                        Unchecked.supplier(() -> jetStreamManagement.pauseConsumer(stream, consumer, pauseUntil))))
                .chain(response -> response.isPaused() ? Uni.createFrom().voidItem()
                        : Uni.createFrom()
                        .failure(() -> new RuntimeException(
                                String.format("Consumer %s in stream %s not paused", consumer, stream))))
                .runSubscriptionOn(this.configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Void> resumeConsumer(@NonNull final String stream, @NonNull final String consumer) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.resumeConsumer(stream, consumer))))
                .chain(response -> response ? Uni.createFrom().voidItem()
                        : Uni.createFrom()
                        .failure(() -> new RuntimeException(
                                String.format("Consumer %s in stream %s not resumed", consumer, stream))))
                .runSubscriptionOn(this.configuration.executorService())
                .emitOn(this::runOnContext);
    }

    private Uni<ConsumerInfo> createConsumer(final String stream, final ConsumerConfiguration configuration) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(
                                () -> jetStreamManagement.createConsumer(stream, map(configuration)))))
                .map(ConsumerInfo::of)
                .runSubscriptionOn(this.configuration.executorService())
                .emitOn(this::runOnContext);
    }

    private io.nats.client.api.ConsumerConfiguration map(final ConsumerConfiguration configuration) {
        var builder = io.nats.client.api.ConsumerConfiguration.builder();
        if (configuration.durable()) {
            builder = builder.durable(configuration.name());
        }
        builder = configuration.filterSubject().map(builder::filterSubject).orElse(builder);
        builder = builder.filterSubjects(configuration.filterSubjects().stream().toList());
        builder = builder.name(configuration.name());
        builder = builder.ackPolicy(AckPolicy.Explicit);
        builder = configuration.acknowledgeWait().map(builder::ackWait).orElse(builder);
        builder = builder.deliverPolicy(configuration.deliverPolicy());
        builder = configuration.startSequence().map(builder::startSequence).orElse(builder);
        builder = configuration.startTime().map(builder::startTime).orElse(builder);
        builder = configuration.description().map(builder::description).orElse(builder);
        builder = configuration.inactiveThreshold().map(builder::inactiveThreshold).orElse(builder);
        builder = configuration.maxAcknowledgePending().map(builder::maxAckPending).orElse(builder);
        builder = configuration.maxDeliver().map(builder::maxDeliver).orElse(builder);
        builder = builder.replayPolicy(configuration.replayPolicy());
        builder = configuration.replicas().map(builder::numReplicas).orElse(builder);
        builder = configuration.memoryStorage().map(builder::memStorage).orElse(builder);
        builder = configuration.sampleFrequency().map(builder::sampleFrequency).orElse(builder);
        if (!configuration.metadata().isEmpty()) {
            builder = builder.metadata(configuration.metadata());
        }
        builder = builder.backoff(configuration.backoff().toArray(new Duration[0]));
        final var pullConfiguration = configuration.pullConfiguration();
        builder = pullConfiguration.maxWaiting().map(builder::maxPullWaiting).orElse(builder);
        builder = pullConfiguration.maxRequestExpires().map(builder::maxExpires).orElse(builder);
        builder = pullConfiguration.maxRequestBatch().map(builder::maxBatch).orElse(builder);
        builder = pullConfiguration.maxRequestMaxBytes().map(builder::maxBytes).orElse(builder);
        return builder.build();
    }

    private Uni<JetStreamManagement> jetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStreamManagement))
                .map(JetStreamManagement::of);
    }

    private void runOnContext(Runnable action) {
        context.runOnContext(action);
    }
}
