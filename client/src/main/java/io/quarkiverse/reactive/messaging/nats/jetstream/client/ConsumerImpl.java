package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.nats.client.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageHeaders;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.NativeMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.Operation;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.Tracer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

@SuppressWarnings("ReactiveStreamsUnusedPublisher")
class ConsumerImpl implements Consumer {
    private final NativeConnection connection;
    private final Context context;
    private final Serializer serializer;
    private final Tracer tracer;

    ConsumerImpl(@NonNull final NativeConnection connection,
            @NonNull final Context context,
            @NonNull final Serializer serializer,
            @NonNull final TracerFactory tracerFactory) {
        this.connection = connection;
        this.context = context;
        this.serializer = serializer;
        this.tracer = tracerFactory.create(Operation.RECEIVE);
    }

    @Override
    public <T> @NonNull Uni<Message<T>> next(@NonNull final String stream,
            @NonNull final String consumer,
            @NonNull final Duration timeout) {
        return consumerContext(stream, consumer)
                .chain(consumerContext -> next(consumerContext, timeout))
                .onItem().ifNotNull().transformToUni(tracer::withTrace)
                .onItem().ifNotNull().<Message<T>> transform(Unchecked.function(message -> deserialize(message)))
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull <T> Uni<Message<T>> next(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout,
            @NonNull Class<T> clazz) {
        return consumerContext(stream, consumer)
                .chain(consumerContext -> next(consumerContext, timeout))
                .onItem().ifNotNull().transformToUni(tracer::withTrace)
                .onItem().ifNotNull().transform(Unchecked.function(message -> deserialize(message, clazz)))
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull <T> Multi<Message<T>> fetch(@NonNull final String stream,
            @NonNull final String consumer,
            @NonNull final Duration timeout, final int batchSize) {
        return subscription(stream, consumer)
                .onItem().transformToMulti(subscription -> fetch(subscription, timeout, batchSize))
                .onItem().transformToUni(tracer::withTrace).concatenate()
                .onItem().<Message<T>> transform(Unchecked.function(message -> deserialize(message)))
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull <T> Multi<Message<T>> fetch(@NonNull String stream,
            @NonNull String consumer,
            @NonNull Duration timeout,
            int batchSize,
            @NonNull Class<T> clazz) {
        return subscription(stream, consumer)
                .onItem().transformToMulti(subscription -> fetch(subscription, timeout, batchSize))
                .onItem().transformToUni(tracer::withTrace).concatenate()
                .onItem().transform(Unchecked.function(message -> deserialize(message, clazz)))
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull <T> Multi<Message<T>> subscribe(final @NonNull String stream,
            final @NonNull String consumer,
            final @NonNull Duration timeout,
            final int batchSize) {
        final var executorService = Executors.newSingleThreadExecutor(ConsumerWorkerThread::new);
        return subscription(stream, consumer)
                .onItem().transformToMulti(subscription -> Multi.createBy().repeating()
                        .uni(() -> Uni.createFrom().item(42))
                        .whilst(v -> true)
                        .onItem().transformToMultiAndConcatenate(v -> fetch(subscription, timeout, batchSize)))
                .select().where(Objects::nonNull)
                .onItem().transformToUni(tracer::withTrace).concatenate()
                .onItem().<Message<T>> transform(Unchecked.function(message -> deserialize(message)))
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(executorService)
                .emitOn(this::runOnContext)
                .onTermination().invoke(executorService::shutdown);
    }

    @Override
    public @NonNull <T> Multi<Message<T>> subscribe(@NonNull String stream,
            @NonNull String consumer, @NonNull Duration timeout, int batchSize,
            @NonNull Class<T> clazz) {
        final var executorService = Executors.newSingleThreadExecutor(ConsumerWorkerThread::new);
        return subscription(stream, consumer)
                .onItem().transformToMulti(subscription -> Multi.createBy().repeating()
                        .uni(() -> Uni.createFrom().item(42))
                        .whilst(v -> true)
                        .onItem().transformToMultiAndConcatenate(v -> fetch(subscription, timeout, batchSize)))
                .select().where(Objects::nonNull)
                .onItem().transformToUni(tracer::withTrace).concatenate()
                .onItem().transform(Unchecked.function(message -> deserialize(message, clazz)))
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(executorService)
                .emitOn(this::runOnContext)
                .onTermination().invoke(executorService::shutdown);
    }

    @Override
    public @NonNull <T> Multi<Message<T>> subscribe(@NonNull String stream,
            @NonNull String consumer) {
        final var executorService = Executors.newSingleThreadExecutor(ConsumerWorkerThread::new);
        final var consumerManagement = new ConsumerManagementImpl(stream, connection, context);
        return Uni.combine().all().unis(jetStream(), dispatcher(), consumerManagement.consumer(consumer))
                .asTuple()
                .onItem()
                .transformToMulti(tuple -> subscribe(stream, consumer, tuple.getItem1(), tuple.getItem2(),
                        tuple.getItem3().configuration()))
                .select().where(Objects::nonNull)
                .onItem().transformToUni(tracer::withTrace).concatenate()
                .onItem().<Message<T>> transform(Unchecked.function(message -> deserialize(message)))
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(executorService)
                .emitOn(this::runOnContext)
                .onTermination().invoke(executorService::shutdown);
    }

    @Override
    public @NonNull <T> Multi<Message<T>> subscribe(@NonNull String stream,
            @NonNull String consumer,
            @NonNull Class<T> clazz) {

        final var executorService = Executors.newSingleThreadExecutor(ConsumerWorkerThread::new);
        final var consumerManagement = new ConsumerManagementImpl(stream, connection, context);
        return Uni.combine().all().unis(jetStream(), dispatcher(), consumerManagement.consumer(consumer))
                .asTuple()
                .onItem()
                .transformToMulti(tuple -> subscribe(stream, consumer, tuple.getItem1(), tuple.getItem2(),
                        tuple.getItem3().configuration()))
                .select().where(Objects::nonNull)
                .onItem().transformToUni(tracer::withTrace).concatenate()
                .onItem().transform(Unchecked.function(message -> deserialize(message, clazz)))
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(executorService)
                .emitOn(this::runOnContext)
                .onTermination().invoke(executorService::shutdown);
    }

    private @NonNull Uni<NativeConsumerContext> consumerContext(@NonNull final String stream, @NonNull final String consumer) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(
                        Unchecked.supplier(() -> jetStream.getConsumerContext(stream, consumer))))
                .map(NativeConsumerContext::of);
    }

    private @NonNull Uni<NativeSubscription> subscription(@NonNull final String stream, @NonNull final String consumer) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(
                        Unchecked.supplier(() -> jetStream.subscribe(null, PullSubscribeOptions.bind(stream, consumer)))))
                .map(NativeSubscription::of);
    }

    private @NonNull Uni<Message<byte[]>> next(
            @NonNull final NativeConsumerContext consumerContext, @NonNull final Duration timeout) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                final var message = consumerContext.next(timeout);
                if (message != null) {
                    emitter.complete(io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message.of(
                            NativeMessage.of(message),
                            context,
                            ConsumerConfiguration.of(consumerContext.getConsumerInfo().getConsumerConfiguration())));
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

    private @NonNull Multi<Message<byte[]>> fetch(
            @NonNull final NativeSubscription subscription,
            @Nullable final Duration timeout,
            final int batchSize) {
        return Multi.createFrom().emitter(emitter -> {
            try {
                final var consumerConfiguration = ConsumerConfiguration
                        .of(subscription.getConsumerInfo().getConsumerConfiguration());
                final var iterator = subscription.iterate(batchSize, timeout);
                while (iterator.hasNext()) {
                    emitter.emit(io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message.of(
                            NativeMessage.of(iterator.next()),
                            context,
                            consumerConfiguration));
                }
                emitter.complete();
            } catch (IllegalStateException e) {
                emitter.complete(); // when the connection is closed
            } catch (Exception failure) {
                emitter.fail(failure);
            }
        });
    }

    @SuppressWarnings("resource")
    private Uni<Dispatcher> dispatcher() {
        return Uni.createFrom().item(Unchecked.supplier(connection()::createDispatcher));
    }

    private @NonNull Multi<Message<byte[]>> subscribe(
            @NonNull String stream,
            @NonNull String consumer,
            @NonNull NativeJetStream jetStream,
            @NonNull Dispatcher dispatcher,
            @NonNull ConsumerConfiguration configuration) {
        return Multi.createFrom().emitter(emitter -> {
            try {
                jetStream.subscribe(
                        null,
                        dispatcher,
                        (MessageHandler) msg -> emitter
                                .emit(io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message.of(
                                        NativeMessage.of(msg),
                                        context,
                                        configuration)),
                        false,
                        PushSubscribeOptions.bind(stream, consumer));
            } catch (Exception e) {
                emitter.fail(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T> @NonNull Message<T> deserialize(@NonNull final Message<byte[]> message) {
        final var m = payloadType(message)
                .map(payloadType -> (Class<T>) payloadType)
                .map(payloadType -> deserialize(message, payloadType))
                .orElseGet(() -> (Message<T>) message);
        return m;
    }

    private <T> @NonNull Message<T> deserialize(@NonNull final Message<byte[]> message, @NonNull Class<T> payloadType) {
        return Message.of(deserialize(message.getPayload(), payloadType), message.getMetadata(), message.getAckWithMetadata(),
                message.getNackWithMetadata());
    }

    private <T> @Nullable T deserialize(byte @Nullable [] payload, @NonNull Class<T> payloadType) {
        return payload != null ? serializer.readValue(payload, payloadType) : null;
    }

    private <T> @NonNull Optional<Class<T>> payloadType(final @NonNull Message<byte[]> message) {
        return message.getMetadata(MessageHeaders.class).flatMap(this::payloadType);
    }

    private <T> @NonNull Optional<Class<T>> payloadType(@NonNull final Headers headers) {
        return headers.payloadType();
    }

    @SuppressWarnings("resource")
    private @NonNull Uni<NativeJetStream> jetStream() {
        return Uni.createFrom().item(Unchecked.supplier(connection()::jetStream))
                .map(NativeJetStreamDelegate::new);
    }

    private @NonNull NativeConnection connection() {
        return connection;
    }

    private void runOnContext(@NonNull Runnable action) {
        context.runOnContext(action);
    }

    private @NonNull ExecutorService executorService() {
        return context.executorService();
    }
}
