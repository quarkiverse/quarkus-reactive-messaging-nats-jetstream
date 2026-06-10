package io.quarkiverse.reactive.messaging.nats.client.stream;

import java.util.HashSet;

import io.nats.client.api.StreamInfo;
import io.quarkiverse.reactive.messaging.nats.client.ClientException;
import io.quarkiverse.reactive.messaging.nats.client.api.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.client.api.StreamResult;
import io.quarkiverse.reactive.messaging.nats.client.api.StreamState;
import io.quarkiverse.reactive.messaging.nats.client.api.StreamStatus;
import io.quarkiverse.reactive.messaging.nats.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.client.connection.JetStreamAware;
import io.quarkiverse.reactive.messaging.nats.client.context.ContextAware;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class StreamAwareImpl extends ContextAware implements StreamAware, JetStreamAware {
    private final StreamStateMapper streamStateMapper;
    private final StreamConfigurationMapper streamConfigurationMapper;
    private final Connection connection;

    public StreamAwareImpl(ExecutionHolder executionHolder,
            StreamStateMapper streamStateMapper, StreamConfigurationMapper streamConfigurationMapper, Connection connection) {
        super(executionHolder);
        this.streamStateMapper = streamStateMapper;
        this.streamConfigurationMapper = streamConfigurationMapper;
        this.connection = connection;
    }

    @Override
    public Uni<Long> firstSequence(final String streamName) {
        return withContext(context -> context.execute(streamInfo(streamName)
                .onItem().transform(Unchecked.function(streamInfo -> streamInfo.getStreamState().getFirstSequence()))
                .onFailure().transform(ClientException::new)));
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public Multi<String> streamNames() {
        return withContext(context -> context.execute(jetStreamManagement(connection)
                .onItem()
                .transformToMulti(jetStreamManagement -> Multi.createFrom()
                        .items(Unchecked.supplier(() -> jetStreamManagement.getStreamNames().stream())))
                .onFailure().transform(ClientException::new)));
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public Multi<String> subjects(final String streamName) {
        return withContext(context -> context.execute(streamInfo(streamName)
                .onItem().transform(Unchecked.function(streamInfo -> streamInfo.getConfiguration().getSubjects()))
                .onItem().transformToMulti(subjects -> Multi.createFrom().items(subjects.stream()))
                .onFailure().transform(ClientException::new)));
    }

    @Override
    public Uni<PurgeResult> purge(final String streamName) {
        return withContext(context -> context.execute(jetStreamManagement(connection)
                .onItem()
                .transformToUni(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.purgeStream(streamName))))
                .onItem().transform(response -> new PurgeResult(streamName, response.isSuccess(), response.getPurged()))
                .onFailure().transform(ClientException::new)));
    }

    @Override
    public Uni<Void> deleteMessage(final String stream, final long sequence, final boolean erase) {
        return withContext(context -> context.execute(jetStreamManagement(connection)
                .onItem()
                .transformToUni(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.deleteMessage(stream, sequence, erase))))
                .onItem()
                .transformToUni(deleted -> deleted ? Uni.createFrom().voidItem()
                        : Uni.createFrom().failure(() -> new MessageNotDeletedException(stream, sequence)))
                .onFailure().transform(ClientException::new)));
    }

    @Override
    public Uni<StreamState> streamState(final String streamName) {
        return withContext(context -> context.execute(streamInfo(streamName))
                .onItem().transform(Unchecked.function(streamInfo -> streamStateMapper.map(streamInfo.getStreamState())))
                .onFailure().transform(ClientException::new));
    }

    @Override
    public Uni<StreamConfiguration> streamConfiguration(final String streamName) {
        return withContext(context -> context.execute(streamInfo(streamName))
                .onItem()
                .transform(Unchecked.function(streamInfo -> streamConfigurationMapper.map(streamInfo.getConfiguration())))
                .onFailure().transform(ClientException::new));
    }

    @Override
    public Multi<PurgeResult> purgeAll() {
        return streamNames()
                .onItem().transformToUniAndMerge(this::purge)
                .onFailure().transform(ClientException::new);
    }

    @Override
    public Uni<StreamResult> addStreamIfAbsent(final StreamConfiguration configuration) {
        return withContext(context -> context.execute(streamNames().collect().asList()
                .onItem().transformToUni(streamNames -> {
                    if (streamNames.contains(configuration.name())) {
                        return streamInfo(configuration.name()).onItem().transform(streamInfo -> StreamResult.builder()
                                .configuration(configuration).status(StreamStatus.NotModified).build());
                    } else {
                        return addStream(streamConfigurationMapper.map(configuration)).onItem()
                                .transform(jsm -> StreamResult.builder().configuration(configuration)
                                        .status(StreamStatus.Created).build());
                    }
                })
                .onFailure().transform(ClientException::new)));
    }

    @Override
    public Uni<Void> addSubject(final String streamName, final String subject) {
        return withContext(context -> context.execute(streamInfo(streamName)
                .onItem().transform(Unchecked.function(streamInfo -> {
                    final var subjects = new HashSet<>(streamInfo.getConfiguration().getSubjects());
                    if (!subjects.contains(subject)) {
                        subjects.add(subject);
                        return io.nats.client.api.StreamConfiguration
                                .builder(streamInfo.getConfiguration())
                                .subjects(subjects)
                                .build();
                    }
                    return null;
                }))
                .onItem().ifNotNull().transformToUni(this::updateStream)
                .onItem().<Void> transform(streamInfo -> null)
                .onFailure().transform(ClientException::new)));
    }

    @Override
    public Uni<Void> removeSubject(final String streamName, final String subject) {
        return withContext(context -> context.execute(streamInfo(streamName)
                .onItem().transform(Unchecked.function(streamInfo -> {
                    final var subjects = new HashSet<>(streamInfo.getConfiguration().getSubjects());
                    if (subjects.contains(subject)) {
                        subjects.remove(subject);
                        return io.nats.client.api.StreamConfiguration
                                .builder(streamInfo.getConfiguration())
                                .subjects(subjects)
                                .build();
                    }
                    return null;
                }))
                .onItem().ifNotNull().transformToUni(this::updateStream)
                .onItem().<Void> transform(streamInfo -> null)
                .onFailure().transform(ClientException::new)));
    }

    private Uni<StreamInfo> streamInfo(final String streamName) {
        return jetStreamManagement(connection)
                .onItem()
                .transformToUni(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.getStreamInfo(streamName))));
    }

    private Uni<StreamInfo> addStream(final io.nats.client.api.StreamConfiguration config) {
        return jetStreamManagement(connection)
                .onItem()
                .transformToUni(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.addStream(config))));
    }

    private Uni<StreamInfo> updateStream(final io.nats.client.api.StreamConfiguration config) {
        return jetStreamManagement(connection)
                .onItem()
                .transformToUni(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.updateStream(config))));
    }
}
