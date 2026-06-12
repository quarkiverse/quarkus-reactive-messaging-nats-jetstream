package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.nats.client.JetStreamStatusException;
import io.nats.client.PublishOptions;
import io.nats.client.PullSubscribeOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.NativeConsumerContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.NativeSubscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.SubscriptionWorkerThread;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing.Tracer;
import io.quarkiverse.reactive.messaging.nats.jetstream.stream.NativeStreamContext;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@JBossLog
@RequiredArgsConstructor
class VertxClient implements Client {
    private final ClientConfiguration configuration;
    private final NativeConnection connection;
    private final Context context;
    private final Tracer publishTracer;
    private final Tracer consumerTracer;

    @Override
    public @NonNull Uni<Message> publish(@NonNull final Message message, @NonNull final String stream, @NonNull final String subject) {
        return withMetadata(message, stream, subject)
                .chain(publishTracer::withTrace)
                .chain(this::publish)
                .chain(this::acknowledge)
                .onFailure().recoverWithUni(failure -> notAcknowledge(message, failure))
                .runSubscriptionOn(configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<Message> publish(@NonNull final Multi<Message> messages, @NonNull final String stream, @NonNull final String subject) {
        return messages.onItem().transformToUniAndMerge(message -> publish(message, stream, subject));
    }

    @Override
    public @NonNull Uni<Message> next(@NonNull final String stream, @NonNull final String consumer, @NonNull final Duration timeout) {
        return consumerContext(stream, consumer)
                .chain(consumerContext -> next(consumerContext, timeout))
                .onItem().ifNotNull().transform(NativeMessage::of)
                .onItem().ifNotNull().transform(message -> Message.of(message, context))
                .runSubscriptionOn(configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<Message> fetch(@NonNull final String stream, @NonNull final String consumer, @NonNull final Duration timeout, final int batchSize) {
        return subscription(stream, consumer)
                .onItem().transformToMulti(subscription -> fetch(subscription, timeout, batchSize))
                .onItem().transform(message -> Message.of(message, context))
                .onItem().transformToUniAndMerge(consumerTracer::withTrace)
                .runSubscriptionOn(configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<MessageInfo> messageInfo(@NonNull final String stream, final long sequence) {
        return streamContext(stream)
                .chain(streamContext -> Uni.createFrom().item(Unchecked.supplier(() -> streamContext.getMessage(sequence))))
                .map(MessageInfo::of)
                .runSubscriptionOn(configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<Message> subscribe(@NonNull final String stream, @NonNull final String consumer, @NonNull final Duration timeout, final int batchSize) {
        final ExecutorService executorService = Executors.newSingleThreadExecutor(SubscriptionWorkerThread::new);
        return subscription(stream, consumer)
                .onItem().transformToMulti(subscription -> Multi.createBy().repeating()
                        .uni(() -> Uni.createFrom().item(42))
                        .whilst(v -> true)
                        .onItem().transformToMultiAndConcatenate(v -> fetch(subscription, timeout, batchSize)))
                .select().where(Objects::nonNull)
                .onItem().transform(message -> Message.of(message, context))
                .onItem().transformToUniAndMerge(consumerTracer::withTrace)
                .runSubscriptionOn(executorService)
                .emitOn(this::runOnContext)
                .onTermination().invoke(executorService::shutdown);
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    private Uni<Message> publish(final Message message) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    final var headers = message.getMetadata(Headers.class).orElseThrow(() -> new IllegalArgumentException("Headers is required"));
                    return jetStream.publish(
                            headers.subject().orElseThrow(() -> new IllegalArgumentException("Subject header is required")),
                            headers.to(),
                            message.getPayload(),
                            PublishOptions.builder()
                                    .messageId(headers.messageId().orElseThrow(() -> new IllegalArgumentException("MessageId is required")))
                                    .expectedStream(headers.stream().orElseThrow(() -> new IllegalArgumentException("Stream header is required")))
                                    .build());
                })))
                .map(Unchecked.function(publishAck -> {
                    final var metadata = message.getMetadata().with(AcknowledgeMetadata.of(publishAck));
                    return (Message) message.withMetadata(metadata);
                }));
    }

    private Uni<Message> withMetadata(final Message message, final String stream, final String subject) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            final var headers = message.getMetadata(Headers.class).orElseThrow(() -> new IllegalArgumentException("Headers is required"));
            if (headers.messageId().isEmpty()) {
                headers.setMessageId(UUID.randomUUID().toString());
            }
            headers.setStream(stream);
            headers.setSubject(subject);
            return (Message) message.addMetadata(configuration.message()).addMetadata(headers);
        }));
    }

    private Uni<NativeJetStream> jetStream() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStream))
                .map(NativeJetStreamDelegate::new);
    }

    private Uni<Message> acknowledge(final Message message) {
        return Uni.createFrom().completionStage(message.ack())
                .onItem().transform(v -> message);
    }

    private Uni<Message> notAcknowledge(final Message message, final Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(throwable))
                .onItem().invoke(() -> log.warnf(throwable, "Message not acknowledged: %s", throwable.getMessage()))
                .chain(v -> Uni.createFrom().item(message));
    }

    private void runOnContext(Runnable action) {
        context.runOnContext(action);
    }

    private Uni<NativeConsumerContext> consumerContext(final String stream, final String consumer) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(
                        Unchecked.supplier(() -> jetStream.getConsumerContext(stream, consumer))))
                .map(NativeConsumerContext::of);
    }

    private Uni<NativeSubscription> subscription(final String stream, final String consumer) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(
                        Unchecked.supplier(() -> jetStream.subscribe(null, PullSubscribeOptions.bind(stream, consumer)))))
                .map(NativeSubscription::of);
    }

    private Uni<io.nats.client.Message> next(final NativeConsumerContext consumerContext, final Duration timeout) {
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

    private Multi<NativeMessage> fetch(final NativeSubscription subscription, @Nullable final Duration timeout, final int batchSize) {
        return Multi.createFrom().<io.nats.client.Message>emitter(emitter -> {
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

    private Uni<NativeStreamContext> streamContext(@NonNull final String streamName) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(Unchecked.supplier(() -> jetStream.getStreamContext(streamName))))
                .map(NativeStreamContext::of);
    }
}
