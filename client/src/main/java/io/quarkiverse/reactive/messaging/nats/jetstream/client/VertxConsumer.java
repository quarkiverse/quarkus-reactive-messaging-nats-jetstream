package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.nats.client.JetStreamStatusException;
import io.nats.client.PullSubscribeOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.NativeMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.Tracer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamContext;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class VertxConsumer implements Consumer {
    private final VertxClient client;
    private final Tracer tracer;
    private final ConsumerConfigurationMapper mapper;

    @Override
    public @NonNull Uni<Message> next(@NonNull final String stream, @NonNull final String consumer,
            @NonNull final Duration timeout) {
        return consumerContext(stream, consumer)
                .chain(consumerContext -> next(consumerContext, timeout))
                .onItem().ifNotNull().transform(this::toMessage)
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<Message> fetch(@NonNull final String stream, @NonNull final String consumer,
            @NonNull final Duration timeout, final int batchSize) {
        return subscription(stream, consumer)
                .onItem().transformToMulti(subscription -> fetch(subscription, timeout, batchSize))
                .onItem().transform(this::toMessage)
                .onItem().transformToUniAndMerge(tracer::withTrace)
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<MessageInfo> message(@NonNull final String stream, final long sequence) {
        return streamContext(stream)
                .chain(streamContext -> Uni.createFrom().item(Unchecked.supplier(() -> streamContext.getMessage(sequence))))
                .map(MessageInfo::of)
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<Message> subscribe(@NonNull final String stream, @NonNull final String consumer,
            @NonNull final Duration timeout, final int batchSize) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor(SubscriptionWorkerThread::new);
        return subscription(stream, consumer)
                .onItem().transformToMulti(subscription -> Multi.createBy().repeating()
                        .uni(() -> Uni.createFrom().item(42))
                        .whilst(v -> true)
                        .onItem().transformToMultiAndConcatenate(v -> fetch(subscription, timeout, batchSize)))
                .select().where(Objects::nonNull)
                .onItem().transform(this::toMessage)
                .onItem().transformToUniAndMerge(tracer::withTrace)
                .runSubscriptionOn(executorService)
                .emitOn(this::runOnContext)
                .onTermination().invoke(executorService::shutdown);
    }

    @Override
    public @NonNull Uni<ConsumerInfo> consumer(@NonNull final String stream, @NonNull final String consumer) {
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
                .onItem().ifNotNull().transform(ConsumerInfo::of)
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<ConsumerInfo> consumers(@NonNull final String stream) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.getConsumerNames(stream))))
                .onItem().transformToMulti(consumers -> Multi.createFrom().items(consumers.stream()))
                .onItem().transformToUniAndMerge(consumer -> consumer(stream, consumer))
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    private Uni<ConsumerContext> consumerContext(final String stream, final String consumer) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(
                        Unchecked.supplier(() -> jetStream.getConsumerContext(stream, consumer))))
                .map(ConsumerContext::of);
    }

    private Uni<Subscription> subscription(final String stream, final String consumer) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(
                        Unchecked.supplier(() -> jetStream.subscribe(null, PullSubscribeOptions.bind(stream, consumer)))))
                .map(Subscription::of);
    }

    private Uni<Tuple2<NativeMessage, ConsumerConfiguration>> next(final ConsumerContext consumerContext,
            final Duration timeout) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                final var message = consumerContext.next(timeout);
                if (message != null) {
                    emitter.complete(Tuple2.of(NativeMessage.of(message),
                            mapper.map(consumerContext.getConsumerInfo().getConsumerConfiguration())));
                } else {
                    emitter.complete(null);
                }
            } catch (JetStreamStatusException e) {
                emitter.fail(e);
            } catch (IllegalStateException | InterruptedException e) {
                emitter.complete(null);
            } catch (Exception e) {
                emitter.fail(e);
            }
        });
    }

    private Multi<Tuple2<NativeMessage, ConsumerConfiguration>> fetch(final Subscription subscription,
            @Nullable final Duration timeout, final int batchSize) {
        return Multi.createFrom().emitter(emitter -> {
            try {
                final var consumerConfiguration = mapper.map(subscription.getConsumerInfo().getConsumerConfiguration());
                final var iterator = subscription.iterate(batchSize, timeout);
                while (iterator.hasNext()) {
                    emitter.emit(Tuple2.of(NativeMessage.of(iterator.next()), consumerConfiguration));
                }
                emitter.complete();
            } catch (IllegalStateException e) {
                emitter.complete(); // when the connection is closed
            } catch (Exception failure) {
                emitter.fail(failure);
            }
        });
    }

    private Uni<StreamContext> streamContext(@NonNull final String streamName) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(Unchecked.supplier(() -> jetStream.getStreamContext(streamName))))
                .map(StreamContext::of);
    }

    @SuppressWarnings("resource")
    private Uni<JetStream> jetStream() {
        return Uni.createFrom().item(Unchecked.supplier(connection()::jetStream))
                .map(JetStreamDelegate::new);
    }

    @SuppressWarnings("resource")
    private Uni<JetStreamManagement> jetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection()::jetStreamManagement))
                .map(JetStreamManagement::of);
    }

    private Message toMessage(final Tuple2<NativeMessage, ConsumerConfiguration> tuple) {
        return Message.of(tuple.getItem1(), context(), tuple.getItem2());
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
