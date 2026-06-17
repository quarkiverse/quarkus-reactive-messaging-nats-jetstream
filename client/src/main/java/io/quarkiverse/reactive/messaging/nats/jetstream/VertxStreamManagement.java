package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.quarkiverse.reactive.messaging.nats.jetstream.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.ConsumerConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.ConsumerInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.stream.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.stream.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.stream.StreamInfo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.time.ZonedDateTime;

@RequiredArgsConstructor
class VertxStreamManagement implements StreamManagement {
    private final ClientConfiguration configuration;
    private final Connection connection;
    private final Context context;
    private final Consumer consumer;
    private final ConsumerConfigurationMapper consumerConfigurationMapper;

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

    @Override
    public Uni<PurgeResult> purge(String streamName) {
        return null;
    }

    @Override
    public Uni<Long> firstSequence(String streamName) {
        return null;
    }

    @Override
    public Uni<Void> deleteMessage(String stream, long sequence, boolean erase) {
        return null;
    }

    @Override
    public Multi<PurgeResult> purgeAll() {
        return null;
    }

    @Override
    public Uni<Void> addSubject(String streamName, String subject) {
        return null;
    }

    @Override
    public Uni<Void> removeSubject(String streamName, String subject) {
        return null;
    }

    @Override
    public Uni<StreamInfo> addStreamIfAbsent(StreamConfiguration configuration) {
        return null;
    }

    private Uni<ConsumerInfo> createConsumer(final String stream, final ConsumerConfiguration configuration) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(
                                () -> jetStreamManagement.createConsumer(stream, consumerConfigurationMapper.map(configuration)))))
                .map(ConsumerInfo::of)
                .runSubscriptionOn(this.configuration.executorService())
                .emitOn(this::runOnContext);
    }

    private Uni<JetStreamManagement> jetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStreamManagement))
                .map(JetStreamManagement::of);
    }

    private void runOnContext(Runnable action) {
        context.runOnContext(action);
    }
}
