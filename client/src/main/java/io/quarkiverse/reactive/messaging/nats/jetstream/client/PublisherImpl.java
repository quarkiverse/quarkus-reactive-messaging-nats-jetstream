package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.nats.client.JetStream;
import io.nats.client.PublishOptions;
import io.nats.client.api.PublishAck;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.AcknowledgeMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageHeaders;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.PublishHeaders;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.Operation;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.Tracer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
class PublisherImpl implements Publisher {
    private final NativeConnection connection;
    private final Context context;
    private final Serializer serializer;
    private final Tracer tracer;
    private final Tracer acknowledgeTracer;

    PublisherImpl(@NonNull final NativeConnection connection,
            @NonNull final Context context,
            @NonNull final Serializer serializer,
            @NonNull final TracerFactory tracerFactory) {
        this.connection = connection;
        this.context = context;
        this.serializer = serializer;
        this.tracer = tracerFactory.create(Operation.PUBLISH);
        this.acknowledgeTracer = tracerFactory.create(Operation.PUBLISH_ACKNOWLEDGED);
    }

    @Override
    public <T> @NonNull Uni<Message<T>> publish(
            @NonNull final Message<T> message,
            @NonNull final String stream,
            @NonNull final String subject) {
        return serializedWithMetadata(message, stream, subject)
                .chain(tracer::withTrace)
                .chain(this::publish)
                .chain(acknowledgeTracer::withTrace)
                .chain(this::acknowledge)
                .map(m -> map(message.getPayload(), m))
                .onFailure().recoverWithUni(failure -> notAcknowledge(message, failure))
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    private <T> @NonNull Message<T> map(final @Nullable T payload, final @NonNull Message<byte[]> message) {
        return Message.of(payload,
                message.getMetadata(), message.getAckWithMetadata(), message.getNackWithMetadata());
    }

    private Uni<Message<byte[]>> publish(@NonNull final Message<byte[]> message) {
        return jetStream()
                .chain(jetStream -> publish(jetStream, message))
                .map(Unchecked.function(publishAck -> {
                    final var metadata = message.getMetadata().with(AcknowledgeMetadata.of(publishAck));
                    return message.withMetadata(metadata);
                }));
    }

    private Uni<PublishAck> publish(@NonNull JetStream jetStream, @NonNull final Message<byte[]> message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
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
        }));
    }

    private <T> @NonNull Uni<Message<byte[]>> serializedWithMetadata(@NonNull final Message<T> message,
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

            if (message.getPayload() != null) {
                headers.setPayloadType(message.getPayload().getClass());
            }

            final var metadata = message.getMetadata(LocalContextMetadata.class)
                    .map(localContextMetadata -> message.getMetadata().with(headers))
                    .orElseGet(() -> message.getMetadata().with(headers).with(captureContextMetadata(message.getMetadata())));

            return Message.of(serializer.toBytes(message.getPayload()),
                    metadata,
                    message.getAckWithMetadata(),
                    message.getNackWithMetadata());
        }));
    }

    private <T> @NonNull String subject(@NonNull final Message<T> message, @NonNull final String subject) {
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

    private <T> @NonNull Uni<Message<T>> acknowledge(final @NonNull Message<T> message) {
        return Uni.createFrom().completionStage(message.ack())
                .onItem().transform(v -> message);
    }

    private <T> @NonNull Uni<Message<T>> notAcknowledge(@NonNull Message<T> message, @NonNull final Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(new PublishException(throwable)))
                .map(ignore -> message)
                .onFailure().invoke(() -> log.warnf(throwable, "Message not acknowledged: %s", throwable.getMessage()));
    }

    private @NonNull Uni<NativeJetStream> jetStream() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStream))
                .map(NativeJetStreamDelegate::new);
    }

    private void runOnContext(@NonNull Runnable action) {
        context.runOnContext(action);
    }

    private @NonNull ExecutorService executorService() {
        return context.executorService();
    }
}
