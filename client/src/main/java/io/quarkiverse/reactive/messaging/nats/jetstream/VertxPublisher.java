package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.nats.client.PublishOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.AcknowledgeMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing.Tracer;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

@JBossLog
@RequiredArgsConstructor
class VertxPublisher implements Publisher {
    private final @NonNull Client client;
    private final @NonNull Tracer tracer;

    @Override
    public @NonNull Uni<Message> publish(@NonNull final Message message, @NonNull final String stream,
            @NonNull final String subject) {
        return withMetadata(message, stream, subject)
                .chain(tracer::withTrace)
                .chain(this::publish)
                .chain(this::acknowledge)
                .onFailure().recoverWithUni(failure -> notAcknowledge(message, failure))
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<Message> publish(@NonNull final Multi<Message> messages, @NonNull final String stream,
            @NonNull final String subject) {
        return messages.onItem().transformToUniAndMerge(message -> publish(message, stream, subject));
    }

    private Uni<Message> publish(final Message message) {
        return jetStream()
                .chain(jetStream -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    final var headers = message.getMetadata(Headers.class)
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
                    return (Message) message.withMetadata(metadata);
                }));
    }

    private Uni<Message> withMetadata(final Message message, final String stream, final String subject) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            final var headers = message.getMetadata(Headers.class)
                    .orElseThrow(() -> new IllegalArgumentException("Headers is required"));
            if (headers.messageId().isEmpty()) {
                headers.setMessageId(UUID.randomUUID().toString());
            }
            headers.setStream(stream);
            headers.setSubject(subject);
            return (Message) message.addMetadata(configuration().messageConfiguration()).addMetadata(headers);
        }));
    }

    private Uni<JetStream> jetStream() {
        return Uni.createFrom().item(Unchecked.supplier(connection()::jetStream))
                .map(JetStreamDelegate::new);
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

    private ClientConfiguration configuration() {
        return client.configuration();
    }

    private Connection connection() {
        return client.connection();
    }

    private void runOnContext(Runnable action) {
        client.context().runOnContext(action);
    }

}
