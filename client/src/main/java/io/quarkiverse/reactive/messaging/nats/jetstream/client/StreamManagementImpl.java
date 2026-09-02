package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.api.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamManagement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamManagementException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.Stream;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.StreamConfiguration;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class StreamManagementImpl implements StreamManagement {
    private final ClientImpl client;

    @Override
    public @NonNull Uni<PurgeResult> purge(@NonNull final String stream) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.purgeStream(stream))))
                .onItem().transform(response -> PurgeResult.of(stream, response))
                .onFailure().transform(StreamManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Void> deleteMessage(@NonNull final String stream, final long sequence, final boolean erase) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.deleteMessage(stream, sequence, erase))))
                .chain(deleted -> deleted ? Uni.createFrom().voidItem()
                        : Uni.createFrom()
                                .failure(() -> new RuntimeException(
                                        String.format("Message with sequence %s not deleted in stream %s", sequence, stream))))
                .onFailure().transform(StreamManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<PurgeResult> purgeAll() {
        return streamNames()
                .onItem().transformToMulti(streams -> Multi.createFrom().iterable(streams))
                .onItem().transformToUniAndMerge(this::purge)
                .onFailure().transform(StreamManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Stream> addSubject(@NonNull final String stream, @NonNull final String subject) {
        return stream(stream)
                .chain(streamInfo -> addSubject(streamInfo, subject))
                .onFailure().transform(StreamManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Stream> removeSubject(@NonNull final String stream, @NonNull final String subject) {
        return stream(stream)
                .chain(streamInfo -> removeSubject(streamInfo, subject))
                .onFailure().transform(StreamManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Stream> addIfAbsent(@NonNull StreamConfiguration configuration) {
        return streamNames().chain(streamNames -> {
            if (streamNames.contains(configuration.name())) {
                return stream(configuration.name());
            } else {
                return addStream(configuration);
            }
        })
                .onFailure().transform(StreamManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Message> message(@NonNull final String stream, final long sequence) {
        return streamContext(stream)
                .chain(streamContext -> Uni.createFrom().item(Unchecked.supplier(() -> streamContext.getMessage(sequence))))
                .map(Message::of)
                .onFailure().transform(StreamManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Stream> stream(@NonNull final String stream) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.getStreamInfo(stream))))
                .onItem().ifNotNull().transform(Stream::of)
                .onFailure().transform(StreamManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<Stream> streams() {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(jetStreamManagement::getStreams)))
                .onItem().transformToMulti(streams -> Multi.createFrom().iterable(streams))
                .onItem().transform(Stream::of)
                .onFailure().transform(StreamManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @SuppressWarnings("resource")
    private @NonNull Uni<NativeJetStreamManagement> jetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection()::jetStreamManagement))
                .map(NativeJetStreamManagement::of);
    }

    private @NonNull Uni<Set<String>> streamNames() {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(jetStreamManagement::getStreamNames)))
                .onItem().ifNotNull().transform(HashSet::new);
    }

    private @NonNull Uni<Stream> addStream(@NonNull final StreamConfiguration configuration) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked
                                .supplier(() -> jetStreamManagement.addStream(StreamConfiguration.of(configuration)))))
                .map(Stream::of);
    }

    private @NonNull Uni<StreamContext> streamContext(@NonNull final String streamName) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(Unchecked.supplier(() -> jetStream.getStreamContext(streamName))))
                .map(StreamContext::of);
    }

    private @NonNull Uni<Stream> addSubject(@NonNull final Stream stream, @NonNull final String subject) {
        if (stream.streamState().subjects().stream().anyMatch(streamSubject -> streamSubject.name().equals(subject))) {
            return Uni.createFrom().item(stream);
        } else {
            final var subjects = new ArrayList<>(stream.configuration().subjects());
            subjects.add(subject);
            return updateStream(StreamConfiguration.of(stream.configuration(), subjects));
        }
    }

    private @NonNull Uni<Stream> removeSubject(@NonNull final Stream stream, @NonNull final String subject) {
        final var subjects = new ArrayList<>(stream.configuration().subjects());
        subjects.remove(subject);
        return updateStream(StreamConfiguration.of(stream.configuration(), subjects));
    }

    private @NonNull Uni<Stream> updateStream(@NonNull final StreamConfiguration configuration) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(
                                () -> jetStreamManagement.updateStream(StreamConfiguration.of(configuration)))))
                .map(Stream::of);
    }

    @SuppressWarnings("resource")
    private @NonNull Uni<NativeJetStream> jetStream() {
        return Uni.createFrom().item(Unchecked.supplier(connection()::jetStream))
                .map(NativeJetStreamDelegate::new);
    }

    private @NonNull NativeConnection connection() {
        return client.connection();
    }

    private void runOnContext(@NonNull Runnable action) {
        client.clientContext().runOnContext(action);
    }

    private @NonNull ExecutorService executorService() {
        return client.clientContext().executorService();
    }
}
