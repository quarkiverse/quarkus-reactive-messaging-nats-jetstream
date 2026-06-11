package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.nats.client.JetStreamStatusException;
import io.nats.client.PublishOptions;
import io.nats.client.PullSubscribeOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.NativeConsumerContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.NativeSubscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing.Tracer;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.UUID;

@JBossLog
@RequiredArgsConstructor
class VertxClient implements Client {
    private final ClientConfiguration configuration;
    private final NativeConnection connection;
    private final Context context;
    private final Tracer publishTracer;

    @Override
    public @NonNull Uni<Message> publish(@NonNull Message message, @NonNull String stream, @NonNull String subject) {
        return withMetadata(message, stream, subject)
                .chain(publishTracer::withTrace)
                .chain(this::publish)
                .chain(this::acknowledge)
                .onFailure().recoverWithUni(failure -> notAcknowledge(message, failure))
                .runSubscriptionOn(configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<Message> publish(@NonNull Multi<Message> messages, @NonNull String stream, @NonNull String subject) {
        return messages.onItem().transformToUniAndMerge(message -> publish(message, stream, subject));
    }

    @Override
    public @NonNull Uni<Message> next(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout) {
        return consumerContext(stream, consumer)
                .chain(consumerContext -> next(consumerContext, timeout))
                .onItem().ifNotNull().transform(NativeMessage::of)
                .onItem().ifNotNull().transform(message -> Message.of(message, context))
                .runSubscriptionOn(configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    private Uni<Message> publish(final Message message) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    final var publishMetadata = message.getMetadata(PublishMetadata.class).orElseThrow(() -> new RuntimeException("PublishMetadata is required"));
                    final var headers = message.getMetadata(Headers.class).orElseThrow(() -> new RuntimeException("Headers is required"));
                    return jetStream.publish(
                            publishMetadata.subject(),
                            headers.to(),
                            message.getPayload(),
                            PublishOptions.builder()
                                    .messageId(headers.messageId().orElseThrow(() -> new RuntimeException("MessageId is required")))
                                    .expectedStream(publishMetadata.stream())
                                    .build());
                })))
                .map(Unchecked.function(publishAck -> {
                    final var metadata = message.getMetadata().with(AcknowledgeMetadata.of(publishAck));
                    return (Message) message.withMetadata(metadata);
                }));
    }

    private Uni<Message> withMetadata(final Message message, final String stream, final String subject) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            final var publishMetadata = PublishMetadata.of(stream, subject);
            final var headers = message.getMetadata(Headers.class).orElseThrow(() -> new RuntimeException("Headers is required"));
            if (headers.messageId().isEmpty()) {
                headers.setMessageId(UUID.randomUUID().toString());
            }
            return (Message) message.addMetadata(publishMetadata)
                    .addMetadata(configuration.message())
                    .addMetadata(headers);
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
}
