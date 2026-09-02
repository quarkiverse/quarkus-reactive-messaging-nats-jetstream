package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.nats.client.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.Operation;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.Tracer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValue;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueManagement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectStore;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectStoreManagement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamManagement;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.extern.jbosslog.JBossLog;

@SuppressWarnings("DuplicatedCode")
@JBossLog
class ClientImpl implements Client {
    private final NativeConnection connection;
    private final StreamManagement streamManagement;
    private final KeyValueManagement keyValueManagement;
    private final ObjectStoreManagement objectStoreManagement;

    private final Tracer publisherTracer;
    private final Tracer publishAcknowledgedTracer;
    private final Tracer consumerTracer;
    private final ClientContext clientContext;
    private final MessageMapper messageMapper;

    ClientImpl(@NonNull NativeConnection connection,
            @NonNull ClientContext clientContext,
            @NonNull TracerFactory tracerFactory,
            @NonNull MessageMapper messageMapper) {
        this.connection = connection;

        this.publisherTracer = tracerFactory.create(Operation.PUBLISH);
        this.publishAcknowledgedTracer = tracerFactory.create(Operation.PUBLISH_ACKNOWLEDGED);
        this.consumerTracer = tracerFactory.create(Operation.RECEIVE);
        this.clientContext = clientContext;
        this.messageMapper = messageMapper;

        this.streamManagement = new StreamManagementImpl(this);
        this.keyValueManagement = new KeyValueManagementImpl(this);
        this.objectStoreManagement = new ObjectStoreManagementImpl(this);
    }

    @Override
    public <T> @NonNull Uni<org.eclipse.microprofile.reactive.messaging.Message<T>> publish(
            org.eclipse.microprofile.reactive.messaging.@NonNull Message<T> message, @NonNull final String stream,
            @NonNull final String subject) {
        return withMetadata(messageMapper.map(message), stream, subject)
                .chain(publisherTracer::withTrace)
                .chain(this::publish)
                .chain(publishAcknowledgedTracer::withTrace)
                .map(m -> message.withMetadata(m.getMetadata()))
                .chain(this::acknowledge)
                .onFailure().recoverWithUni(failure -> notAcknowledge(message, failure))
                .runSubscriptionOn(clientContext.executorService())
                .emitOn(clientContext::runOnContext);
    }

    @Override
    public <T> @NonNull Uni<org.eclipse.microprofile.reactive.messaging.Message<T>> next(@NonNull final String stream,
            @NonNull final String consumer,
            @NonNull final Duration timeout) {
        return consumerContext(stream, consumer)
                .chain(consumerContext -> next(consumerContext, timeout))
                .onItem().ifNotNull().transform(this::toMessage)
                .onItem().ifNotNull().transformToUni(consumerTracer::withTrace)
                .onItem().ifNotNull().<org.eclipse.microprofile.reactive.messaging.Message<T>> transform(messageMapper::map)
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(clientContext.executorService())
                .emitOn(clientContext::runOnContext);
    }

    @Override
    public @NonNull <T> Uni<org.eclipse.microprofile.reactive.messaging.Message<T>> next(@NonNull String stream,
            @NonNull String consumer, @NonNull Duration timeout, @NonNull Class<T> clazz) {
        return consumerContext(stream, consumer)
                .chain(consumerContext -> next(consumerContext, timeout))
                .onItem().ifNotNull().transform(this::toMessage)
                .onItem().ifNotNull().transformToUni(consumerTracer::withTrace)
                .onItem().ifNotNull().transform(message -> messageMapper.map(message, clazz))
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(clientContext.executorService())
                .emitOn(clientContext::runOnContext);
    }

    @Override
    public @NonNull <T> Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> fetch(@NonNull final String stream,
            @NonNull final String consumer,
            @NonNull final Duration timeout, final int batchSize) {
        return subscription(stream, consumer)
                .onItem().transformToMulti(subscription -> fetch(subscription, timeout, batchSize))
                .onItem().transform(this::toMessage)
                .onItem().transformToUni(consumerTracer::withTrace).concatenate()
                .onItem().<org.eclipse.microprofile.reactive.messaging.Message<T>> transform(messageMapper::map)
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(clientContext.executorService())
                .emitOn(clientContext::runOnContext);
    }

    @Override
    public @NonNull <T> Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> fetch(@NonNull String stream,
            @NonNull String consumer, @NonNull Duration timeout, int batchSize, @NonNull Class<T> clazz) {
        return subscription(stream, consumer)
                .onItem().transformToMulti(subscription -> fetch(subscription, timeout, batchSize))
                .onItem().transform(this::toMessage)
                .onItem().transformToUni(consumerTracer::withTrace).concatenate()
                .onItem().transform(message -> messageMapper.map(message, clazz))
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(clientContext.executorService())
                .emitOn(clientContext::runOnContext);
    }

    @Override
    public @NonNull <T> Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> subscribe(final @NonNull String stream,
            final @NonNull String consumer,
            final @NonNull Duration timeout, final int batchSize) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor(ConsumerWorkerThread::new);
        return subscription(stream, consumer)
                .onItem().transformToMulti(subscription -> Multi.createBy().repeating()
                        .uni(() -> Uni.createFrom().item(42))
                        .whilst(v -> true)
                        .onItem().transformToMultiAndConcatenate(v -> fetch(subscription, timeout, batchSize)))
                .select().where(Objects::nonNull)
                .onItem().transform(this::toMessage)
                .onItem().transformToUni(consumerTracer::withTrace).concatenate()
                .onItem().<org.eclipse.microprofile.reactive.messaging.Message<T>> transform(messageMapper::map)
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(executorService)
                .emitOn(clientContext::runOnContext)
                .onTermination().invoke(executorService::shutdown);
    }

    @Override
    public @NonNull <T> Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> subscribe(@NonNull String stream,
            @NonNull String consumer, @NonNull Duration timeout, int batchSize,
            @NonNull Class<T> clazz) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor(ConsumerWorkerThread::new);
        return subscription(stream, consumer)
                .onItem().transformToMulti(subscription -> Multi.createBy().repeating()
                        .uni(() -> Uni.createFrom().item(42))
                        .whilst(v -> true)
                        .onItem().transformToMultiAndConcatenate(v -> fetch(subscription, timeout, batchSize)))
                .select().where(Objects::nonNull)
                .onItem().transform(this::toMessage)
                .onItem().transformToUni(consumerTracer::withTrace).concatenate()
                .onItem().transform(message -> messageMapper.map(message, clazz))
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(executorService)
                .emitOn(clientContext::runOnContext)
                .onTermination().invoke(executorService::shutdown);
    }

    @Override
    public @NonNull <T> Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> subscribe(@NonNull String stream,
            @NonNull String consumer) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor(ConsumerWorkerThread::new);
        return Uni.combine().all().unis(jetStream(), dispatcher(), consumerManagement(stream).consumer(consumer)).asTuple()
                .onItem()
                .transformToMulti(tuple -> subscribe(stream, consumer, tuple.getItem1(), tuple.getItem2(),
                        tuple.getItem3().configuration()))
                .select().where(Objects::nonNull)
                .onItem().transform(this::toMessage)
                .onItem().transformToUni(consumerTracer::withTrace).concatenate()
                .onItem().<org.eclipse.microprofile.reactive.messaging.Message<T>> transform(messageMapper::map)
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(executorService)
                .emitOn(clientContext::runOnContext)
                .onTermination().invoke(executorService::shutdown);
    }

    @Override
    public @NonNull <T> Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> subscribe(@NonNull String stream,
            @NonNull String consumer, @NonNull Class<T> clazz) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor(ConsumerWorkerThread::new);
        return Uni.combine().all().unis(jetStream(), dispatcher(), consumerManagement(stream).consumer(consumer)).asTuple()
                .onItem()
                .transformToMulti(tuple -> subscribe(stream, consumer, tuple.getItem1(), tuple.getItem2(),
                        tuple.getItem3().configuration()))
                .select().where(Objects::nonNull)
                .onItem().transform(this::toMessage)
                .onItem().transformToUni(consumerTracer::withTrace).concatenate()
                .onItem().transform(message -> messageMapper.map(message, clazz))
                .onFailure().transform(SubscriptionException::new)
                .runSubscriptionOn(executorService)
                .emitOn(clientContext::runOnContext)
                .onTermination().invoke(executorService::shutdown);
    }

    @Override
    public @NonNull StreamManagement streamManagement() {
        return streamManagement;
    }

    @Override
    public @NonNull ConsumerManagement consumerManagement(@NonNull String stream) {
        return new ConsumerManagementImpl(stream, this);
    }

    @Override
    public @NonNull KeyValueManagement keyValueManagement() {
        return keyValueManagement;
    }

    @Override
    public @NonNull ObjectStoreManagement objectStoreManagement() {
        return objectStoreManagement;
    }

    @Override
    public @NonNull ObjectStore objectStore(@NonNull final String bucketName) {
        return new ObjectStoreImpl(bucketName, this);
    }

    @Override
    public @NonNull KeyValue keyValue(@NonNull final String bucketName) {
        return new KeyValueImpl(bucketName, this);
    }

    @Override
    public boolean closed() {
        return connection.getStatus() == io.nats.client.Connection.Status.CLOSED;
    }

    @NonNull
    NativeConnection connection() {
        return connection;
    }

    @Override
    public void close() throws Exception {
        if (connection.getStatus() != io.nats.client.Connection.Status.CLOSED) {
            connection.close();
        }
    }

    @NonNull
    ClientContext clientContext() {
        return clientContext;
    }

    private Uni<Message> publish(@NonNull final Message message) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    final var headers = message.getMetadata(PublishHeaders.class)
                            .orElseThrow(() -> new IllegalArgumentException("Headers is required"));
                    return jetStream.publish(
                            headers.subject().orElseThrow(() -> new IllegalArgumentException("Subject header is required")),
                            headers.to(),
                            message.getPayload(),
                            PublishOptions.builder()
                                    .messageId(headers.messageId()
                                            .orElseThrow(() -> new IllegalArgumentException("MessageId is required")))
                                    .expectedStream(headers.stream()
                                            .orElseThrow(() -> new IllegalArgumentException("Stream header is required")))
                                    .build());
                })))
                .map(Unchecked.function(publishAck -> {
                    final var metadata = message.getMetadata().with(AcknowledgeMetadata.of(publishAck));
                    return message.withMetadata(metadata);
                }));
    }

    private @NonNull Uni<Message> withMetadata(@NonNull final Message message,
            @NonNull final String stream,
            @NonNull final String subject) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            final var headers = message.getMetadata(PublishHeaders.class).orElseGet(PublishHeaders::of);
            if (headers.messageId().isEmpty()) {
                headers.setMessageId(UUID.randomUUID().toString());
            }
            headers.setStream(stream);
            headers.setSubject(subject(message, subject));

            message.getMetadata(MessageHeaders.class)
                    .flatMap(MessageHeaders::correlationId)
                    .ifPresent(headers::setCorrelationId);

            return Message.of(message, headers);
        }));
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

    private @NonNull Uni<Tuple2<NativeMessage, ConsumerConfiguration>> next(
            @NonNull final NativeConsumerContext consumerContext, @NonNull final Duration timeout) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                final var message = consumerContext.next(timeout);
                if (message != null) {
                    emitter.complete(Tuple2.of(NativeMessage.of(message),
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

    private @NonNull Multi<Tuple2<NativeMessage, ConsumerConfiguration>> fetch(@NonNull final NativeSubscription subscription,
            @Nullable final Duration timeout,
            final int batchSize) {
        return Multi.createFrom().emitter(emitter -> {
            try {
                final var consumerConfiguration = ConsumerConfiguration
                        .of(subscription.getConsumerInfo().getConsumerConfiguration());
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

    private @NonNull Message toMessage(@NonNull final Tuple2<NativeMessage, ConsumerConfiguration> tuple) {
        return Message.of(tuple.getItem1(), clientContext, tuple.getItem2());
    }

    @SuppressWarnings("resource")
    private @NonNull Uni<NativeJetStream> jetStream() {
        return Uni.createFrom().item(Unchecked.supplier(connection()::jetStream))
                .map(NativeJetStreamDelegate::new);
    }

    private <T> @NonNull Uni<org.eclipse.microprofile.reactive.messaging.Message<T>> acknowledge(
            final org.eclipse.microprofile.reactive.messaging.@NonNull Message<T> message) {
        return Uni.createFrom().completionStage(message.ack())
                .onItem().transform(v -> message);
    }

    private <T> @NonNull Uni<org.eclipse.microprofile.reactive.messaging.Message<T>> notAcknowledge(
            org.eclipse.microprofile.reactive.messaging.@NonNull Message<T> message, @NonNull final Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(new PublishException(throwable)))
                .map(ignore -> message)
                .onFailure().invoke(() -> log.warnf(throwable, "Message not acknowledged: %s", throwable.getMessage()));
    }

    private @NonNull String subject(@NonNull final Message message, @NonNull final String subject) {
        // Replier auto-routing: a reply flowing from an incoming channel carries the requestor's advertised reply
        // subject; it wins over the channel-configured subject (which may only be a prefix).
        final var replySubject = message.getMetadata(MessageHeaders.class).flatMap(MessageHeaders::replySubject);
        if (replySubject.isPresent()) {
            return replySubject.get();
        }
        final var result = message.getMetadata(PublishHeaders.class)
                .flatMap(PublishHeaders::subject)
                .orElse(subject);
        if (!result.startsWith(subject)) {
            throw new IllegalArgumentException("Subject must start with " + subject);
        }
        return result;
    }

    private Uni<Dispatcher> dispatcher() {
        return Uni.createFrom().item(Unchecked.supplier(connection::createDispatcher));
    }

    private @NonNull Multi<Tuple2<NativeMessage, ConsumerConfiguration>> subscribe(@NonNull String stream,
            @NonNull String consumer,
            @NonNull NativeJetStream jetStream,
            @NonNull Dispatcher dispatcher,
            @NonNull ConsumerConfiguration configuration) {
        return Multi.createFrom().emitter(emitter -> {
            try {
                jetStream.subscribe(
                        null,
                        dispatcher,
                        (MessageHandler) msg -> emitter.emit(Tuple2.of(NativeMessage.of(msg), configuration)),
                        false,
                        PushSubscribeOptions.bind(stream, consumer));
            } catch (Exception e) {
                emitter.fail(e);
            }
        });
    }
}
