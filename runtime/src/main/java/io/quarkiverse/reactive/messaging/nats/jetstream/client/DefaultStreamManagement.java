package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import io.nats.client.ConsumerContext;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.StreamInfo;
import io.nats.client.api.StreamInfoOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfigurationFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.StreamConfigurationFactory;
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
    public Uni<List<String>> getStreams() {
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
        return context().executeBlocking(getStreams()
                .onItem().transformToUni(this::purgeAllStreams));
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public Uni<List<StreamResult>> addStreams(List<StreamSetupConfiguration> streamConfigurations) {
        return context().executeBlocking(getJetStreamManagement()
                .onItem()
                .transformToMulti(jetStreamManagement -> Multi.createFrom()
                        .items(streamConfigurations.stream()
                                .map(streamConfiguration -> StreamSetupConfigurationTuple.builder()
                                        .jetStreamManagement(jetStreamManagement).configuration(streamConfiguration).build())))
                .onItem().transformToUniAndMerge(tuple -> addOrUpdateStream(tuple.jetStreamManagement(), tuple.configuration()))
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

    private Uni<StreamResult> addOrUpdateStream(final JetStreamManagement jsm,
            final StreamSetupConfiguration setupConfiguration) {
        return getStreamInfo(jsm, setupConfiguration.configuration().name())
                .onItem()
                .transformToUni(tuple -> updateStream(tuple.jetStreamManagement(), tuple.streamInfo(), setupConfiguration))
                .onFailure().recoverWithUni(failure -> createStream(jsm, setupConfiguration.configuration()));
    }

    private Uni<StreamResult> createStream(final JetStreamManagement jsm,
            final StreamConfiguration streamConfiguration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var factory = new StreamConfigurationFactory();
                final var streamConfig = factory.create(streamConfiguration);
                jsm.addStream(streamConfig);
                return StreamResult.builder()
                        .configuration(streamConfiguration)
                        .status(StreamStatus.Created)
                        .build();
            } catch (Exception failure) {
                throw new SetupException(String.format("Unable to create stream: %s with message: %s",
                        streamConfiguration.name(), failure.getMessage()), failure);
            }
        }));
    }

    private Uni<StreamResult> updateStream(final JetStreamManagement jsm,
            final StreamInfo streamInfo,
            final StreamSetupConfiguration setupConfiguration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var currentConfiguration = streamInfo.getConfiguration();
                final var factory = new StreamConfigurationFactory();
                final var configuration = factory.create(currentConfiguration, setupConfiguration.configuration());
                if (configuration.isPresent()) {
                    log.debugf("Updating stream %s", setupConfiguration.configuration().name());
                    jsm.updateStream(configuration.get());
                    return StreamResult.builder().configuration(setupConfiguration.configuration())
                            .status(StreamStatus.Updated).build();
                } else {
                    return StreamResult.builder().configuration(setupConfiguration.configuration())
                            .status(StreamStatus.NotModified).build();
                }
            } catch (Exception failure) {
                log.errorf(failure, "message: %s", failure.getMessage());
                throw new SetupException(String.format("Unable to update stream: %s with message: %s",
                        setupConfiguration.configuration().name(), failure.getMessage()), failure);
            }
        }))
                .onFailure().recoverWithUni(failure -> {
                    if (failure.getCause() instanceof JetStreamApiException && setupConfiguration.overwrite()) {
                        return deleteStream(jsm, setupConfiguration.configuration().name())
                                .onItem().transformToUni(v -> createStream(jsm, setupConfiguration.configuration()));
                    }
                    return Uni.createFrom().failure(failure);
                });
    }

    private Uni<Void> deleteStream(final JetStreamManagement jsm, final String streamName) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                jsm.deleteStream(streamName);
                return null;
            } catch (IOException | JetStreamApiException failure) {
                throw new SetupException(String.format("Unable to delete stream: %s with message: %s", streamName,
                        failure.getMessage()), failure);
            }
        }));
    }

    private <T> Uni<ConsumerContext> addOrUpdateConsumerInternal(ConsumerConfiguration<T> configuration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var factory = new ConsumerConfigurationFactory();
                final var consumerConfiguration = factory.create(configuration);
                final var streamContext = connection.getStreamContext(configuration.stream());
                return streamContext.createOrUpdateConsumer(consumerConfiguration);
            } catch (Throwable failure) {
                throw new SystemException(failure);
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

    @Builder
    private record StreamSetupConfigurationTuple(StreamSetupConfiguration configuration,
            JetStreamManagement jetStreamManagement) {
    }

    @Builder
    private record StreamInfoTuple(StreamInfo streamInfo, JetStreamManagement jetStreamManagement) {
    }
}
