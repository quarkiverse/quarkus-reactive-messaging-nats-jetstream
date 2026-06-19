package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
class VertxStreamManagement implements StreamManagement {
    private final VertxClient client;
    private final ConsumerConfigurationMapper consumerConfigurationMapper;
    private final StreamConfigurationMapper streamConfigurationMapper;
    private final StreamInfoMapper streamInfoMapper;

    @Override
    public @NonNull Uni<ConsumerInfo> addConsumerIfAbsent(@NonNull final String stream, @NonNull final ConsumerConfiguration configuration) {
        return client.consumer(stream, configuration.name())
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
                .runSubscriptionOn(configuration().executorService())
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
                .runSubscriptionOn(configuration().executorService())
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
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<PurgeResult> purge(@NonNull final String stream) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.purgeStream(stream))))
                .onItem().transform(response -> PurgeResult.builder().stream(stream)
                        .success(response.isSuccess()).purgeCount(response.getPurged()).build())
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Void> deleteMessage(@NonNull final String stream, final long sequence, final boolean erase) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.deleteMessage(stream, sequence, erase))))
                .chain(deleted -> deleted ? Uni.createFrom().voidItem()
                        : Uni.createFrom().failure(() -> new RuntimeException(String.format("Message with sequence %s not deleted in stream %s", sequence, stream))))
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<PurgeResult> purgeAll() {
        return streamNames()
                .onItem().transformToMulti(streams -> Multi.createFrom().iterable(streams))
                .onItem().transformToUniAndMerge(this::purge)
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<StreamInfo> addSubject(@NonNull final String stream, @NonNull final String subject) {
        return client.stream(stream)
                .chain(streamInfo -> addSubject(streamInfo, subject))
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Void> removeSubject(@NonNull final String streamName, @NonNull final String subject) {
        return null;
    }

    @Override
    public @NonNull Uni<StreamInfo> addStreamIfAbsent(@NonNull StreamConfiguration configuration) {
        return streamNames().chain(streamNames -> {
                    if (streamNames.contains(configuration.name())) {
                        return client.stream(configuration.name());
                    } else {
                        return addStream(configuration);
                    }
                })
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    private Uni<ConsumerInfo> createConsumer(final String stream, final ConsumerConfiguration configuration) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(
                                () -> jetStreamManagement.createConsumer(stream, consumerConfigurationMapper.map(configuration)))))
                .map(ConsumerInfo::of)
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    private Uni<JetStreamManagement> jetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection()::jetStreamManagement))
                .map(JetStreamManagement::of);
    }

    private Uni<Set<String>> streamNames() {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(jetStreamManagement::getStreamNames)))
                .onItem().ifNotNull().transform(HashSet::new);
    }

    private Uni<StreamInfo> addStream(final StreamConfiguration configuration) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.addStream(streamConfigurationMapper.map(configuration)))))
                .map(streamInfoMapper::map);
    }

    private Uni<StreamInfo> addSubject(final StreamInfo streamInfo, final String subject) {
        if (streamInfo.streamState().subjects().stream().anyMatch(streamSubject -> streamSubject.name().equals(subject))) {
            return Uni.createFrom().item(streamInfo);
        } else {
            final var subjects = new ArrayList<>(streamInfo.config().subjects());
            subjects.add(subject);
            return updateStream(streamConfigurationMapper.map(streamInfo.config(), subjects));
        }
    }

    private Uni<StreamInfo> updateStream(final StreamConfiguration configuration) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() ->
                        jetStreamManagement.updateStream(streamConfigurationMapper.map(configuration)))))
                .map(streamInfoMapper::map);
    }

    private ClientConfiguration configuration() {
        return client.configuration();
    }

    private Connection connection() {
        return client.connection();
    }

    private Context context() {
        return client.context();
    }

    private void runOnContext(Runnable action) {
        context().runOnContext(action);
    }
}
