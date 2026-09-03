package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.ZonedDateTime;
import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerManagement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerManagementException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ConsumerManagementImpl implements ConsumerManagement {
    private final String stream;
    private final ClientImpl client;

    @Override
    public @NonNull Uni<Consumer> addIfAbsent(@NonNull final ConsumerConfiguration configuration) {
        return consumer(configuration.name())
                .onItem().ifNull().switchTo(() -> createConsumer(configuration))
                .onFailure().transform(ConsumerManagementException::new);
    }

    @Override
    public @NonNull Uni<Void> delete(@NonNull final String consumer) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.deleteConsumer(stream, consumer))))
                .chain(deleted -> deleted ? Uni.createFrom().voidItem()
                        : Uni.createFrom()
                                .failure(() -> new RuntimeException(
                                        String.format("Consumer %s in stream %s not deleted", consumer, stream))))
                .onFailure().transform(ConsumerManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Void> pause(@NonNull final String consumer,
            @NonNull final ZonedDateTime pauseUntil) {
        return jetStreamManagement().chain(jetStreamManagement -> Uni.createFrom().item(
                Unchecked.supplier(() -> jetStreamManagement.pauseConsumer(stream, consumer, pauseUntil))))
                .chain(response -> response.isPaused() ? Uni.createFrom().voidItem()
                        : Uni.createFrom()
                                .failure(() -> new RuntimeException(
                                        String.format("Consumer %s in stream %s not paused", consumer, stream))))
                .onFailure().transform(ConsumerManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Void> resume(@NonNull final String consumer) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.resumeConsumer(stream, consumer))))
                .chain(response -> response ? Uni.createFrom().voidItem()
                        : Uni.createFrom()
                                .failure(() -> new RuntimeException(
                                        String.format("Consumer %s in stream %s not resumed", consumer, stream))))
                .onFailure().transform(ConsumerManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Consumer> consumer(@NonNull final String consumer) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.getConsumerNames(stream)))
                        .chain(consumerNames -> {
                            if (consumerNames.contains(consumer)) {
                                return Uni.createFrom()
                                        .item(Unchecked.supplier(() -> jetStreamManagement.getConsumerInfo(stream, consumer)));
                            } else {
                                return Uni.createFrom().nullItem();
                            }
                        }))
                .onItem().ifNotNull().transform(Consumer::of)
                .onFailure().transform(ConsumerManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    public @NonNull Multi<Consumer> consumers() {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.getConsumerNames(stream))))
                .onItem().transformToMulti(consumers -> Multi.createFrom().items(consumers.stream()))
                .onItem().transformToUniAndMerge(this::consumer)
                .onFailure().transform(ConsumerManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @SuppressWarnings("resource")
    private @NonNull Uni<NativeJetStreamManagement> jetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection()::jetStreamManagement))
                .map(NativeJetStreamManagement::of);
    }

    private @NonNull Uni<Consumer> createConsumer(@NonNull final ConsumerConfiguration configuration) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(
                                () -> jetStreamManagement.createConsumer(stream, ConsumerConfiguration.of(configuration)))))
                .map(Consumer::of)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
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
