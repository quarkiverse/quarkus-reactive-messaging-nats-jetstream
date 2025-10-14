package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamState;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.ConsumerMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.StreamStateMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

@RequiredArgsConstructor
@JBossLog
class DefaultStreamManagement implements StreamManagement {
    private final io.nats.client.Connection connection;
    private final StreamStateMapper streamStateMapper;
    private final ConsumerMapper consumerMapper;
    private final Vertx vertx;

    @Override
    public Uni<Consumer> getConsumer(String stream, String consumerName) {
        return context().executeBlocking(getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    try {
                        return jsm.getConsumerInfo(stream, consumerName);
                    } catch (IOException | JetStreamApiException e) {
                        throw new SystemException(e);
                    }
                })))
                .onItem().transform(consumerMapper::of));
    }

    @Override
    public Uni<Void> deleteConsumer(String streamName, String consumerName) {
        return context().executeBlocking(getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    try {
                        jsm.deleteConsumer(streamName, consumerName);
                        return null;
                    } catch (IOException | JetStreamApiException failure) {
                        throw new SystemException(failure);
                    }
                }))));
    }

    @Override
    public Uni<Void> pauseConsumer(String streamName, String consumerName, ZonedDateTime pauseUntil) {
        return context().executeBlocking(getJetStreamManagement()
                .onItem().transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    try {
                        final var response = jetStreamManagement.pauseConsumer(streamName, consumerName, pauseUntil);
                        if (!response.isPaused()) {
                            throw new SystemException(
                                    String.format("Unable to pause consumer %s in stream %s", consumerName, streamName));
                        }
                        return null;
                    } catch (IOException | JetStreamApiException failure) {
                        throw new SystemException(failure);
                    }
                }))));
    }

    @Override
    public Uni<Void> resumeConsumer(String streamName, String consumerName) {
        return context().executeBlocking(getJetStreamManagement()
                .onItem().transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    try {
                        final var response = jetStreamManagement.resumeConsumer(streamName, consumerName);
                        if (!response) {
                            throw new SystemException(
                                    String.format("Unable to resume consumer %s in stream %s", consumerName, streamName));
                        }
                        return null;
                    } catch (IOException | JetStreamApiException failure) {
                        throw new SystemException(failure);
                    }
                }))));
    }

    @Override
    public Uni<Long> getFirstSequence(String streamName) {
        return context().executeBlocking(getStreamInfo(streamName)
                .onItem().transform(tuple -> tuple.streamInfo().getStreamState().getFirstSequence()));
    }

    @Override
    public Uni<List<String>> getStreamNames() {
        return context().executeBlocking(getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().item(Unchecked.supplier((jsm::getStreamNames)))));
    }

    @Override
    public Uni<List<String>> getSubjects(String streamName) {
        return context().executeBlocking(getStreamInfo(streamName)
                .onItem().transform(tuple -> tuple.streamInfo().getConfiguration().getSubjects()));
    }

    @Override
    public Uni<List<String>> getConsumerNames(String streamName) {
        return context().executeBlocking(getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    try {
                        return jsm.getConsumerNames(streamName);
                    } catch (IOException | JetStreamApiException failure) {
                        throw new SystemException(failure);
                    }
                }))));
    }

    @Override
    public Uni<PurgeResult> purgeStream(String streamName) {
        return context().executeBlocking(getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    try {
                        final var response = jsm.purgeStream(streamName);
                        return new PurgeResult(streamName, response.isSuccess(), response.getPurged());
                    } catch (IOException | JetStreamApiException failure) {
                        throw new SystemException(failure);
                    }
                }))));
    }

    @Override
    public Uni<Void> deleteMessage(String stream, long sequence, boolean erase) {
        return context().executeBlocking(getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    try {
                        if (!jsm.deleteMessage(stream, sequence, erase)) {
                            throw new DeleteException(
                                    String.format("Unable to delete message in stream %s with sequence %d", stream, sequence));
                        }
                        return null;
                    } catch (IOException | JetStreamApiException failure) {
                        throw new DeleteException(
                                String.format("Unable to delete message in stream %s with sequence %d: %s", stream,
                                        sequence, failure.getMessage()),
                                failure);
                    }
                }))));
    }

    @Override
    public Uni<StreamState> getStreamState(String streamName) {
        return context().executeBlocking(getStreamInfo(streamName)
                .onItem().transform(tuple -> streamStateMapper.of(tuple.streamInfo().getStreamState())));
    }

    @Override
    public Uni<StreamConfiguration> getStreamConfiguration(String streamName) {
        return context().executeBlocking(getStreamInfo(streamName)
                .onItem().transform(tuple -> StreamConfiguration.of(tuple.streamInfo().getConfiguration())));
    }

    @Override
    public Multi<PurgeResult> purgeAllStreams() {
        return getStreamNames()
                .onItem().transformToMulti(this::purgeAllStreams);
    }

    @Override
    public Uni<StreamResult> addStream(String name, StreamConfiguration configuration) {
        return getJetStreamManagement()
                .onItem().transformToUni(jsm -> addStream(jsm, name, configuration));
    }

    @Override
    public Uni<Void> addSubject(String streamName, String subject) {
        return context().executeBlocking(getStreamInfo(streamName)
                .onItem().transformToUni(tuple -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    try {
                        final var subjects = new HashSet<>(tuple.streamInfo().getConfiguration().getSubjects());
                        if (!subjects.contains(subject)) {
                            subjects.add(subject);
                            final var configuration = io.nats.client.api.StreamConfiguration
                                    .builder(tuple.streamInfo().getConfiguration())
                                    .subjects(subjects)
                                    .build();
                            tuple.jetStreamManagement().updateStream(configuration);
                        }
                        return null;
                    } catch (IOException | JetStreamApiException failure) {
                        throw new SystemException(failure);
                    }
                }))));
    }

    @Override
    public Uni<Void> removeSubject(String streamName, String subject) {
        return context().executeBlocking(getStreamInfo(streamName)
                .onItem().transformToUni(tuple -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    try {
                        final var subjects = new HashSet<>(tuple.streamInfo().getConfiguration().getSubjects());
                        if (subjects.contains(subject)) {
                            subjects.remove(subject);
                            final var configuration = io.nats.client.api.StreamConfiguration
                                    .builder(tuple.streamInfo().getConfiguration())
                                    .subjects(subjects)
                                    .build();
                            tuple.jetStreamManagement().updateStream(configuration);
                        }
                        return null;
                    } catch (IOException | JetStreamApiException failure) {
                        throw new SystemException(failure);
                    }
                }))));
    }

    private Uni<StreamInfoTuple> getStreamInfo(String streamName) {
        return getJetStreamManagement()
                .onItem().transformToUni(jsm -> getStreamInfo(jsm, streamName));
    }

    private Uni<StreamInfoTuple> getStreamInfo(JetStreamManagement jsm, String streamName) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                emitter.complete(StreamInfoTuple.builder().jetStreamManagement(jsm)
                        .streamInfo(jsm.getStreamInfo(streamName, StreamInfoOptions.allSubjects())).build());
            } catch (Throwable failure) {
                emitter.fail(new SystemException(
                        String.format("Unable to read stream %s with message: %s", streamName, failure.getMessage()), failure));
            }
        });
    }

    private Uni<StreamResult> addStream(final JetStreamManagement jetStreamManagement, String name,
            StreamConfiguration configuration) {
        return getStreamInfo(jetStreamManagement, name)
                .onItem()
                .transformToUni(tuple -> updateStream(tuple.jetStreamManagement(), tuple.streamInfo(), name, configuration))
                .onFailure().recoverWithUni(failure -> createStream(jetStreamManagement, name, configuration));

    }

    private Uni<StreamResult> createStream(final JetStreamManagement jsm,
            final String name,
            final StreamConfiguration streamConfiguration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var streamConfig = createStreamConfiguration(name, streamConfiguration);
                jsm.addStream(streamConfig);
                return StreamResult.builder()
                        .configuration(streamConfiguration)
                        .status(StreamStatus.Created)
                        .build();
            } catch (Exception failure) {
                throw new SetupException(String.format("Unable to create stream: %s with message: %s",
                        name, failure.getMessage()), failure);
            }
        }));
    }

    private Uni<StreamResult> updateStream(final JetStreamManagement jsm,
            final StreamInfo streamInfo,
            final String name,
            final StreamConfiguration setupConfiguration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var currentConfiguration = streamInfo.getConfiguration();
                final var configuration = createStreamConfiguration(currentConfiguration, name, setupConfiguration);
                if (configuration.isPresent()) {
                    log.debugf("Updating stream %s", name);
                    jsm.updateStream(configuration.get());
                    return StreamResult.builder().name(name).configuration(setupConfiguration)
                            .status(StreamStatus.Updated).build();
                } else {
                    return StreamResult.builder().name(name).configuration(setupConfiguration)
                            .status(StreamStatus.NotModified).build();
                }
            } catch (Exception failure) {
                log.errorf(failure, "message: %s", failure.getMessage());
                throw new SetupException(String.format("Unable to update stream: %s with message: %s",
                        name, failure.getMessage()), failure);
            }
        }));
    }

    private Uni<PurgeResult> purgeStream(JetStreamManagement jetStreamManagement, String streamName) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var response = jetStreamManagement.purgeStream(streamName);
                return PurgeResult.builder()
                        .streamName(streamName)
                        .success(response.isSuccess())
                        .purgeCount(response.getPurged()).build();
            } catch (IOException | JetStreamApiException e) {
                log.warnf(e, "Unable to purge stream %s with message: %s", streamName, e.getMessage());
                return new PurgeResult(streamName, false, 0);
            }
        }));
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    private Multi<PurgeResult> purgeAllStreams(List<String> streams) {
        return getJetStreamManagement()
                .onItem()
                .transformToMulti(jetStreamManagement -> Multi.createFrom()
                        .items(streams.stream().map(streamName -> Tuple2.of(jetStreamManagement, streamName))))
                .onItem().<PurgeResult> transformToUniAndMerge(tuple -> purgeStream(tuple.getItem1(), tuple.getItem2()));
    }

    private Uni<JetStreamManagement> getJetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStreamManagement));
    }

    private Context context() {
        return vertx.getOrCreateContext();
    }

    public io.nats.client.api.StreamConfiguration createStreamConfiguration(String name,
            StreamConfiguration streamConfiguration) {
        var builder = io.nats.client.api.StreamConfiguration.builder()
                .name(name)
                .storageType(streamConfiguration.storageType())
                .retentionPolicy(streamConfiguration.retentionPolicy())
                .replicas(streamConfiguration.replicas())
                .subjects(streamConfiguration.subjects().orElseGet(Set::of))
                .compressionOption(streamConfiguration.compressionOption());

        builder = streamConfiguration.description().map(builder::description).orElse(builder);
        builder = streamConfiguration.maximumConsumers().map(builder::maxConsumers).orElse(builder);
        builder = streamConfiguration.maximumMessages().map(builder::maxMessages).orElse(builder);
        builder = streamConfiguration.maximumMessagesPerSubject().map(builder::maxMessagesPerSubject)
                .orElse(builder);
        builder = streamConfiguration.maximumBytes().map(builder::maxBytes).orElse(builder);
        builder = streamConfiguration.maximumAge().map(builder::maxAge).orElse(builder);
        builder = streamConfiguration.maximumMessageSize().map(builder::maximumMessageSize).orElse(builder);
        builder = streamConfiguration.templateOwner().map(builder::templateOwner).orElse(builder);
        builder = streamConfiguration.discardPolicy().map(builder::discardPolicy).orElse(builder);
        builder = streamConfiguration.duplicateWindow().map(builder::duplicateWindow).orElse(builder);
        builder = streamConfiguration.allowRollup().map(builder::allowRollup).orElse(builder);
        builder = streamConfiguration.allowDirect().map(builder::allowDirect).orElse(builder);
        builder = streamConfiguration.mirrorDirect().map(builder::mirrorDirect).orElse(builder);
        builder = streamConfiguration.denyDelete().map(builder::denyDelete).orElse(builder);
        builder = streamConfiguration.denyPurge().map(builder::denyPurge).orElse(builder);
        builder = streamConfiguration.discardNewPerSubject().map(builder::discardNewPerSubject).orElse(builder);
        builder = streamConfiguration.firstSequence().map(builder::firstSequence).orElse(builder);

        return builder.build();
    }

    private Optional<io.nats.client.api.StreamConfiguration> createStreamConfiguration(
            io.nats.client.api.StreamConfiguration current,
            String name,
            StreamConfiguration streamConfiguration) {
        var currentConfiguration = StreamConfiguration.of(current);
        if (currentConfiguration.equals(streamConfiguration)) {
            return Optional.empty();
        } else {
            return Optional.of(createStreamConfiguration(name, streamConfiguration));
        }
    }

    @Builder
    private record StreamInfoTuple(StreamInfo streamInfo, JetStreamManagement jetStreamManagement) {
    }
}
