package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.nats.client.JetStreamStatusException;
import io.nats.client.PullSubscribeOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.ConsumerContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.ConsumerInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.Subscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.SubscriptionWorkerThread;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.MessageInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.NativeMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing.Tracer;
import io.quarkiverse.reactive.messaging.nats.jetstream.stream.StreamContext;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class VertxConsumer implements Consumer {
    private final ClientConfiguration configuration;
    private final Connection connection;
    private final Context context;
    private final Tracer tracer;

    @Override
    public @NonNull Uni<Message> next(@NonNull final String stream, @NonNull final String consumer,
            @NonNull final Duration timeout) {
        return consumerContext(stream, consumer)
                .chain(consumerContext -> next(consumerContext, timeout))
                .onItem().ifNotNull().transform(NativeMessage::of)
                .onItem().ifNotNull().transform(message -> Message.of(message, context))
                .runSubscriptionOn(configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<Message> fetch(@NonNull final String stream, @NonNull final String consumer,
            @NonNull final Duration timeout, final int batchSize) {
        return subscription(stream, consumer)
                .onItem().transformToMulti(subscription -> fetch(subscription, timeout, batchSize))
                .onItem().transform(message -> Message.of(message, context))
                .onItem().transformToUniAndMerge(tracer::withTrace)
                .runSubscriptionOn(configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<MessageInfo> message(@NonNull final String stream, final long sequence) {
        return streamContext(stream)
                .chain(streamContext -> Uni.createFrom().item(Unchecked.supplier(() -> streamContext.getMessage(sequence))))
                .map(MessageInfo::of)
                .runSubscriptionOn(configuration.executorService())
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
                .onItem().transform(message -> Message.of(message, context))
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
                .runSubscriptionOn(this.configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<ConsumerInfo> consumers(@NonNull final String stream) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.getConsumerNames(stream))))
                .onItem().transformToMulti(consumers -> Multi.createFrom().items(consumers.stream()))
                .onItem().transformToUniAndMerge(consumer -> consumer(stream, consumer))
                .runSubscriptionOn(this.configuration.executorService())
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

    private Uni<io.nats.client.Message> next(final ConsumerContext consumerContext, final Duration timeout) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                emitter.complete(consumerContext.next(timeout));
            } catch (JetStreamStatusException e) {
                emitter.fail(e);
            } catch (IllegalStateException | InterruptedException e) {
                emitter.complete(null);
            } catch (Exception e) {
                emitter.fail(e);
            }
        });
    }

    private Multi<NativeMessage> fetch(final Subscription subscription, @Nullable final Duration timeout, final int batchSize) {
        return Multi.createFrom().<io.nats.client.Message> emitter(emitter -> {
            try {
                final var iterator = subscription.iterate(batchSize, timeout);
                while (iterator.hasNext()) {
                    emitter.emit(iterator.next());
                }
                emitter.complete();
            } catch (IllegalStateException e) {
                emitter.complete(); // when the connection is closed
            } catch (Exception failure) {
                emitter.fail(failure);
            }
        })
                .onItem().transform(NativeMessage::of);
    }

    private Uni<StreamContext> streamContext(@NonNull final String streamName) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(Unchecked.supplier(() -> jetStream.getStreamContext(streamName))))
                .map(StreamContext::of);
    }

    private Uni<JetStream> jetStream() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStream))
                .map(JetStreamDelegate::new);
    }

    private Uni<JetStreamManagement> jetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStreamManagement))
                .map(JetStreamManagement::of);
    }

    private void runOnContext(Runnable action) {
        context.runOnContext(action);
    }
}
