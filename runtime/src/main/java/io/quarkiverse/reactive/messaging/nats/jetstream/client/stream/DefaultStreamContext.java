package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamState;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamStatus;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.context.ContextAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.StreamConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.StreamStateMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import lombok.extern.jbosslog.JBossLog;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;

@JBossLog
public record DefaultStreamContext(ExecutionHolder executionHolder, ConnectionFactory connectionFactory, StreamStateMapper streamStateMapper, StreamConfigurationMapper streamConfigurationMapper) implements StreamContext, ConnectionAware, ContextAware {

    @Override
    public @NonNull ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    @Override
    public @NonNull ExecutionHolder executionHolder() {
        return executionHolder;
    }

    @Override
    public @NonNull Uni<Long> firstSequence(@NonNull final String streamName) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.streamInfo(streamName)
                .onItem().transform(Unchecked.function(streamInfo -> streamInfo.getStreamState().getFirstSequence()))
                .onFailure().transform(ClientException::new))));
    }

    @Override
    public @NonNull Multi<String> streamNames() {
        return withContext(context -> withSubscriberConnection(Connection::streamNames)
                .onFailure().transform(ClientException::new));
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public @NonNull Multi<String> subjects(@NonNull final String streamName) {
        return withContext(context -> withSubscriberConnection(connection -> connection.streamInfo(streamName)
                .onItem().transform(Unchecked.function(streamInfo -> streamInfo.getConfiguration().getSubjects()))
                .onItem().transformToMulti(subjects -> Multi.createFrom().items(subjects.stream()))
                .onFailure().transform(ClientException::new)));
    }

    @Override
    public @NonNull Uni<PurgeResult> purge(String streamName) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.purgeStream(streamName))
                .onItem().transform(response -> new PurgeResult(streamName, response.isSuccess(), response.getPurged()))
                .onFailure().transform(ClientException::new)));
    }

    @Override
    public @NonNull Uni<Void> deleteMessage(@NonNull final String stream, final long sequence, final boolean erase) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.deleteMessage(stream, sequence, erase)
                .onItem().<Void>transform(Unchecked.function(deleted -> {
                    if (!deleted)
                        throw new DeleteException(String.format("Unable to delete message in stream %s with sequence %d", stream, sequence));
                    return null;
                }))
                .onFailure().transform(failure -> new DeleteException(String.format("Unable to delete message in stream %s with sequence %d: %s",
                        stream,
                        sequence,
                        failure.getMessage()),
                        failure))
        )));
    }

    @Override
    public @NonNull Uni<StreamState> streamState(@NonNull final String streamName) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.streamInfo(streamName))
                .onItem().transform(Unchecked.function(streamInfo -> streamStateMapper.of(streamInfo.getStreamState())))
                .onFailure().transform(ClientException::new)));
    }

    @Override
    public @NonNull Uni<StreamConfiguration> configuration(@NonNull final String streamName) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.streamInfo(streamName))
                .onItem().transform(Unchecked.function(streamInfo -> StreamConfiguration.of(streamInfo.getConfiguration())))
                .onFailure().transform(ClientException::new)));
    }

    @Override
    public @NonNull Multi<PurgeResult> purgeAll() {
        return withSubscriberConnection(connection -> connection.streamNames()
                .onItem().transformToUniAndMerge(name -> purgeStream(connection, name))
                .onFailure().transform(ClientException::new));
    }

    @Override
    public @NonNull Uni<StreamResult> addIfAbsent(@NonNull final String name, @NonNull final StreamConfiguration configuration) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.streamNames().collect().asList()
                .onItem().transformToUni(streamNames -> {
                    if (streamNames.contains(name)) {
                        return connection.streamInfo(name).onItem().transform(streamInfo -> StreamResult.builder().name(name).configuration(configuration).status(StreamStatus.NotModified).build());
                    } else {
                        return connection.addStream(streamConfigurationMapper.of(name, configuration)).onItem().transform(jsm -> StreamResult.builder().name(name).configuration(configuration).status(StreamStatus.Created).build());
                    }
                })
                .onFailure().transform(ClientException::new))));
    }

    @Override
    public @NonNull Uni<Void> addSubject(@NonNull final String streamName, @NonNull final String subject) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.streamInfo(streamName)
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
                .onItem().ifNotNull().transformToUni(connection::updateStream)
                .onItem().<Void>transform(streamInfo -> null)
                .onFailure().transform(ClientException::new))));
    }

    @Override
    public @NonNull Uni<Void> removeSubject(@NonNull final String streamName, @NonNull final String subject) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.streamInfo(streamName)
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
                .onItem().ifNotNull().transformToUni(connection::updateStream)
                .onItem().<Void>transform(streamInfo -> null)
                .onFailure().transform(ClientException::new))));
    }

    private @NonNull Uni<PurgeResult> purgeStream(@NonNull final Connection connection, @NonNull final String streamName) {
        return connection.purgeStream(streamName)
                .onItem().transform(Unchecked.function(response -> PurgeResult.builder()
                        .streamName(streamName)
                        .success(response.isSuccess())
                        .purgeCount(response.getPurged()).build()))
                .onFailure().invoke(failure -> log.warnf(failure, "Unable to purge stream %s with message: %s", streamName, failure.getMessage()))
                .onFailure().recoverWithItem(() -> new PurgeResult(streamName, false, 0));
    }
}
