package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.nats.client.JetStream;
import io.nats.client.PublishOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.AcknowledgeMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.MessageConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.PublishMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing.Tracer;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

@JBossLog
@RequiredArgsConstructor
class VertxClient<T> implements Client<T> {
    private final ClientConfiguration<T> configuration;
    private final NativeConnection connection;
    private final Vertx vertx;
    private final Tracer<T> publishTracer;

    @Override
    public @NonNull Uni<Message<T>> publish(@NonNull Message<T> message, @NonNull String stream, @NonNull String subject) {
        return withMetadata(message, stream,subject)
                .chain(publishTracer::withTrace)
                .chain(this::publish)
                .chain(this::acknowledge)
                .onFailure().recoverWithUni(failure -> notAcknowledge(message, failure))
                .runSubscriptionOn(configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<Message<T>> publish(@NonNull Multi<Message<T>> messages, @NonNull String stream, @NonNull String subject) {
        return messages.onItem().transformToUniAndMerge(message -> publish(message, stream, subject));
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    private Uni<Message<T>> publish(final Message<T> message) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    final var publishMetadata = message.getMetadata(PublishMetadata.class).orElseThrow(() -> new RuntimeException("PublishMetadata is required"));
                    final var headers = message.getMetadata(Headers.class).orElseThrow(() -> new RuntimeException("Headers is required"));
                    final var configuration = message.getMetadata(MessageConfiguration.class).orElseThrow(() -> new RuntimeException("Configuration is required"));
                    return jetStream.publish(
                            publishMetadata.subject(),
                            headers.to(),
                            configuration.payloadMapper().toBytes(message.getPayload()),
                            PublishOptions.builder()
                                    .messageId(headers.messageId().orElseThrow(() -> new RuntimeException("MessageId is required")))
                                    .expectedStream(publishMetadata.stream())
                                    .build());
                })))
                .map(Unchecked.function(publishAck -> {
                    final var metadata = message.getMetadata().with(AcknowledgeMetadata.of(publishAck));
                    return message.withMetadata(metadata);
                }));
    }

    private Uni<Message<T>> withMetadata(final Message<T> message, final String stream, final String subject) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            final var publishMetadata = PublishMetadata.of(stream, subject);
            final var headers = message.getMetadata(Headers.class)
                    .orElse(Headers.of(message.getPayload().getClass()));
            if (headers.messageId().isEmpty()) {
                headers.setMessageId(UUID.randomUUID().toString());
            }
            return message.addMetadata(publishMetadata)
                    .addMetadata(configuration.message())
                    .addMetadata(headers);
        }));
    }


    private Uni<JetStream> jetStream() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStream));
    }

    private Uni<Message<T>> acknowledge(final Message<T> message) {
        return Uni.createFrom().completionStage(message.ack())
                .onItem().transform(v -> message);
    }

    private Uni<Message<T>> notAcknowledge(final Message<T> message, final Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(throwable))
                .onItem().invoke(() -> log.warnf(throwable, "Message not acknowledged: %s", throwable.getMessage()))
                .chain(v -> Uni.createFrom().item(message));
    }

    private void runOnContext(Runnable action) {
        vertx.getOrCreateContext().runOnContext(action);
    }
}
