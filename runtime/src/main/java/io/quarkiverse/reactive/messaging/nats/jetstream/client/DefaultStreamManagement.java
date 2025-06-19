package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.StreamInfo;
import io.nats.client.api.StreamInfoOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.ConsumerMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.StreamStateMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
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
    public Uni<List<PurgeResult>> purgeAllStreams() {
        return context().executeBlocking(getStreamNames()
                .onItem().transformToUni(this::purgeAllStreams));
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public Uni<List<StreamResult>> addStreams(Map<String, ? extends StreamConfiguration> streamConfigurations) {
        return context().executeBlocking(getJetStreamManagement()
                .onItem()
                .transformToMulti(jetStreamManagement -> Multi.createFrom()
                        .items(streamConfigurations.entrySet().stream()
                                .map(entry -> StreamConfigurationTuple.builder()
                                        .jetStreamManagement(jetStreamManagement)
                                        .configuration(entry.getValue())
                                        .streamName(entry.getKey())
                                        .build())))
                .onItem().transformToUniAndMerge(tuple -> addStream(tuple.jetStreamManagement(), tuple.streamName(), tuple.configuration()))
                .collect().asList());
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

    private Uni<StreamResult> addStream(final JetStreamManagement jsm,
                                        final String streamName,
                                        final StreamConfiguration setupConfiguration) {
        return getStreamInfo(jsm, streamName)
                .onItem()
                .transformToUni(tuple -> updateStream(tuple.jetStreamManagement(), streamName, tuple.streamInfo(), setupConfiguration))
                .onFailure().recoverWithUni(failure -> createStream(jsm, streamName, setupConfiguration));
    }

    private Uni<StreamResult> createStream(final JetStreamManagement jsm,
                                           final String streamName,
                                           final StreamConfiguration streamConfiguration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var streamConfig = createStreamConfiguration(streamName, streamConfiguration);
                jsm.addStream(streamConfig);
                return StreamResult.builder()
                        .configuration(streamConfiguration)
                        .status(StreamStatus.Created)
                        .build();
            } catch (Exception failure) {
                throw new SetupException(String.format("Unable to create stream: %s with message: %s",
                        streamName, failure.getMessage()), failure);
            }
        }));
    }

    private Uni<StreamResult> updateStream(final JetStreamManagement jsm,
                                           final String streamName,
                                           final StreamInfo streamInfo,
                                           final StreamConfiguration setupConfiguration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
                    try {
                        final var currentConfiguration = streamInfo.getConfiguration();
                        final var configuration = createStreamConfiguration(currentConfiguration, setupConfiguration);
                        if (configuration.isPresent()) {
                            log.debugf("Updating stream %s", streamName);
                            jsm.updateStream(configuration.get());
                            return StreamResult.builder().configuration(setupConfiguration)
                                    .status(StreamStatus.Updated).build();
                        } else {
                            return StreamResult.builder().configuration(setupConfiguration)
                                    .status(StreamStatus.NotModified).build();
                        }
                    } catch (Exception failure) {
                        log.errorf(failure, "message: %s", failure.getMessage());
                        throw new SetupException(String.format("Unable to update stream: %s with message: %s",
                                streamName, failure.getMessage()), failure);
                    }
                }));
    }

    private Optional<PurgeResult> purgeStream(JetStreamManagement jetStreamManagement, String streamName) {
        try {
            final var response = jetStreamManagement.purgeStream(streamName);
            return Optional.of(PurgeResult.builder()
                    .streamName(streamName)
                    .success(response.isSuccess())
                    .purgeCount(response.getPurged()).build());
        } catch (IOException | JetStreamApiException e) {
            log.warnf(e, "Unable to purge stream %s with message: %s", streamName, e.getMessage());
            return Optional.empty();
        }
    }

    private Uni<List<PurgeResult>> purgeAllStreams(List<String> streams) {
        return getJetStreamManagement()
                .onItem().transformToUni(jsm -> Uni.createFrom().item(
                        Unchecked.supplier(
                                () -> streams.stream().flatMap(streamName -> purgeStream(jsm, streamName).stream()).toList())));
    }

    private Uni<JetStreamManagement> getJetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStreamManagement));
    }

    private Context context() {
        return vertx.getOrCreateContext();
    }

    public io.nats.client.api.StreamConfiguration createStreamConfiguration(String streamName, StreamConfiguration streamConfiguration) {
        var builder = io.nats.client.api.StreamConfiguration.builder()
                .name(streamName)
                .storageType(streamConfiguration.storageType())
                .retentionPolicy(streamConfiguration.retentionPolicy())
                .replicas(streamConfiguration.replicas())
                .subjects(streamConfiguration.subjects())
                .compressionOption(streamConfiguration.compressionOption());

        builder = streamConfiguration.description().map(builder::description).orElse(builder);
        builder = streamConfiguration.maximumConsumers().map(builder::maxConsumers).orElse(builder);
        builder = streamConfiguration.maximumMessages().map(builder::maxMessages).orElse(builder);
        builder = streamConfiguration.maximumMessagesPerSubject().map(builder::maxMessagesPerSubject).orElse(builder);
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

    private Optional<io.nats.client.api.StreamConfiguration> createStreamConfiguration(io.nats.client.api.StreamConfiguration current,
                                                                   StreamConfiguration streamConfiguration) {
        var updated = false;
        var builder = io.nats.client.api.StreamConfiguration.builder();
        builder.name(current.getName());
        final var description = compare(current.getDescription(), streamConfiguration.description().orElse(null));
        if (description.isPresent()) {
            builder = builder.description(description.get());
            updated = true;
        }
        final var storageType = compare(current.getStorageType(), streamConfiguration.storageType());
        if (storageType.isPresent()) {
            builder = builder.storageType(storageType.get());
            updated = true;
        }
        final var retentionPolicy = compare(current.getRetentionPolicy(), streamConfiguration.retentionPolicy());
        if (retentionPolicy.isPresent()) {
            builder = builder.retentionPolicy(retentionPolicy.get());
            updated = true;
        }
        final var replicas = compare(current.getReplicas(), streamConfiguration.replicas());
        if (replicas.isPresent()) {
            builder = builder.replicas(replicas.get());
            updated = true;
        }
        final var subjects = compare(current.getSubjects(), streamConfiguration.subjects());
        if (subjects.isPresent()) {
            builder = builder.subjects(subjects.get());
            updated = true;
        }
        final var compressionOption = compare(current.getCompressionOption(), streamConfiguration.compressionOption());
        if (compressionOption.isPresent()) {
            builder = builder.compressionOption(compressionOption.get());
            updated = true;
        }
        final var maximumConsumers = compare(current.getMaxConsumers(), streamConfiguration.maximumConsumers().orElse(null));
        if (maximumConsumers.isPresent()) {
            builder = builder.maxConsumers(maximumConsumers.get());
            updated = true;
        }
        final var maximumMessages = compare(current.getMaxMsgs(), streamConfiguration.maximumMessages().orElse(null));
        if (maximumMessages.isPresent()) {
            builder = builder.maxMessages(maximumMessages.get());
            updated = true;
        }
        final var maximumMessagesPerSubject = compare(current.getMaxMsgsPerSubject(),
                streamConfiguration.maximumMessagesPerSubject().orElse(null));
        if (maximumMessagesPerSubject.isPresent()) {
            builder = builder.maxMessagesPerSubject(maximumMessagesPerSubject.get());
            updated = true;
        }
        final var maximumBytes = compare(current.getMaxBytes(), streamConfiguration.maximumBytes().orElse(null));
        if (maximumBytes.isPresent()) {
            builder = builder.maxBytes(maximumBytes.get());
            updated = true;
        }
        final var maximumAge = compare(current.getMaxAge(), streamConfiguration.maximumAge().orElse(null));
        if (maximumAge.isPresent()) {
            builder = builder.maxAge(maximumAge.get());
            updated = true;
        }
        final var maximumMessageSize = compare(current.getMaximumMessageSize(),
                streamConfiguration.maximumMessageSize().orElse(null));
        if (maximumMessageSize.isPresent()) {
            builder = builder.maximumMessageSize(maximumMessageSize.get());
            updated = true;
        }
        final var templateOwner = compare(current.getTemplateOwner(), streamConfiguration.templateOwner().orElse(null));
        if (templateOwner.isPresent()) {
            builder = builder.templateOwner(templateOwner.get());
            updated = true;
        }
        final var discardPolicy = compare(current.getDiscardPolicy(), streamConfiguration.discardPolicy().orElse(null));
        if (discardPolicy.isPresent()) {
            builder = builder.discardPolicy(discardPolicy.get());
            updated = true;
        }
        final var duplicateWindow = compare(current.getDuplicateWindow(), streamConfiguration.duplicateWindow().orElse(null));
        if (duplicateWindow.isPresent()) {
            builder = builder.duplicateWindow(duplicateWindow.get());
            updated = true;
        }
        final var allowRollup = compare(current.getAllowRollup(), streamConfiguration.allowRollup().orElse(null));
        if (allowRollup.isPresent()) {
            builder = builder.allowRollup(allowRollup.get());
            updated = true;
        }
        final var allowDirect = compare(current.getAllowDirect(), streamConfiguration.allowDirect().orElse(null));
        if (allowDirect.isPresent()) {
            builder = builder.allowDirect(allowDirect.get());
            updated = true;
        }
        final var mirrorDirect = compare(current.getMirrorDirect(), streamConfiguration.mirrorDirect().orElse(null));
        if (mirrorDirect.isPresent()) {
            builder = builder.mirrorDirect(mirrorDirect.get());
            updated = true;
        }
        final var denyDelete = compare(current.getDenyDelete(), streamConfiguration.denyDelete().orElse(null));
        if (denyDelete.isPresent()) {
            builder = builder.denyDelete(denyDelete.get());
            updated = true;
        }
        final var denyPurge = compare(current.getDenyPurge(), streamConfiguration.denyPurge().orElse(null));
        if (denyPurge.isPresent()) {
            builder = builder.denyPurge(denyPurge.get());
            updated = true;
        }
        final var discardNewPerSubject = compare(current.isDiscardNewPerSubject(),
                streamConfiguration.discardNewPerSubject().orElse(null));
        if (discardNewPerSubject.isPresent()) {
            builder = builder.discardNewPerSubject(discardNewPerSubject.get());
            updated = true;
        }
        final var firstSequence = compare(current.getFirstSequence(), streamConfiguration.firstSequence().orElse(null));
        if (firstSequence.isPresent()) {
            builder = builder.firstSequence(firstSequence.get());
            updated = true;
        }
        if (updated) {
            return Optional.of(builder.build());
        } else {
            return Optional.empty();
        }
    }

    private <T> Optional<T> compare(T currentValue, T newValue) {
        if (currentValue != null && newValue != null && !currentValue.equals(newValue)) {
            return Optional.of(newValue);
        } else if (currentValue == null && newValue != null) {
            return Optional.of(newValue);
        } else {
            return Optional.empty();
        }
    }

    private <T> Optional<Collection<T>> compare(Collection<T> currentValue, Collection<T> newValue) {
        if (currentValue != null && newValue != null && !currentValue.containsAll(newValue)) {
            return Optional.of(newValue);
        } else if (currentValue == null && newValue != null) {
            return Optional.of(newValue);
        } else {
            return Optional.empty();
        }
    }

    @Builder
    private record StreamConfigurationTuple(String streamName, StreamConfiguration configuration,
                                            JetStreamManagement jetStreamManagement) {
    }

    @Builder
    private record StreamInfoTuple(StreamInfo streamInfo, JetStreamManagement jetStreamManagement) {
    }
}
