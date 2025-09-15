package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

import io.nats.client.PublishOptions;
import io.nats.client.impl.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.context.ContextAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

@JBossLog
@RequiredArgsConstructor
public class DefaultPublisherContext implements PublisherContext, ContextAware, ConnectionAware {
    private final ExecutionHolder executionHolder;
    private final ConnectionFactory connectionFactory;
    private final TracerFactory tracerFactory;
    private final PayloadMapper payloadMapper;

    @Override
    public @NonNull ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    @Override
    public @NonNull ExecutionHolder executionHolder() {
        return executionHolder;
    }

    @Override
    public @NonNull <T> Uni<Message<T>> publish(@NonNull Message<T> message, @NonNull String stream, @NonNull String subject) {
        return withContext(context -> context.executeBlocking(
                withConnection(connection -> publish(connection, message, stream, subject))));
    }

    @Override
    public @NonNull <T> Uni<Message<T>> publish(@NonNull Message<T> message, @NonNull String stream, @NonNull String subject, @NonNull PublishListener listener) {
        return null;
    }

    @Override
    public @NonNull <T> Multi<Message<T>> publish(@NonNull Multi<Message<T>> messages, @NonNull String stream, @NonNull String subject, @NonNull PublishListener listener) {
        return null;
    }

    @Override
    public @NonNull <T> Multi<Message<T>> publish(@NonNull Multi<Message<T>> messages, @NonNull String stream, @NonNull String subject) {
        return withContext(context -> withSubscriberConnection(connection ->
                messages.onItem().transformToUniAndMerge(message -> publish(connection, message, stream, subject))));
    }

    private @NonNull <T> Uni<Message<T>> publish(@NonNull final Connection connection, @NonNull final Message<T> message, @NonNull final String stream, @NonNull final String subject) {
        return addPublishMetadata(message, stream, subject)
                .onItem().transformToUni(msg -> tracerFactory.<T>create(TracerType.Publish).withTrace(msg, m -> m))
                .onItem().transformToUni(msg -> publish(connection, msg))
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(failure -> notAcknowledge(message, failure))
                .onFailure().transform(failure -> new PublishException(failure.getMessage(), failure));
    }

    private @NonNull <T> Uni<Message<T>> publish(@NonNull Connection connection, @NonNull final Message<T> message) {
        return getMetadata(message).onItem()
                        .transformToUni(metadata -> connection.publish(
                                metadata.subject(),
                                toJetStreamHeaders(metadata.headers()),
                                metadata.payload(),
                                createPublishOptions(metadata.messageId(), metadata.stream())))
                        .onItem().invoke(ack -> log.infof("Message published : %s", ack))
                        .onItem().transform(ignore -> message);
    }

    private <T> Uni<Message<T>> addPublishMetadata(final Message<T> message, final String stream,
                                                   final String subject) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            final var publishMetadata = PublishMessageMetadata.of(message, stream, subject,
                    payloadMapper.of(message.getPayload()));
            final var metadata = message.getMetadata().without(PublishMessageMetadata.class);
            return message.withMetadata(metadata.with(publishMetadata));
        }));
    }

    private <T> Uni<PublishMessageMetadata> getMetadata(final Message<T> message) {
        return Uni.createFrom().item(() -> message.getMetadata(PublishMessageMetadata.class).orElse(null))
                .onItem().ifNull().failWith(() -> new RuntimeException("Metadata not found"));
    }


    private PublishOptions createPublishOptions(final String messageId, final String streamName) {
        return PublishOptions.builder()
                .messageId(messageId)
                .stream(streamName)
                .build();
    }

    private <T> Uni<Message<T>> acknowledge(final Message<T> message) {
        return Uni.createFrom().completionStage(message.ack())
                .onItem().transform(v -> message);
    }

    private <T> Uni<Message<T>> notAcknowledge(final Message<T> message, final Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(throwable))
                .onItem().invoke(() -> log.warnf(throwable, "Message not acknowledged: %s", throwable.getMessage()))
                .onItem().transformToUni(v -> Uni.createFrom().item(message));
    }

    private Headers toJetStreamHeaders(Map<String, List<String>> headers) {
        final var result = new Headers();
        headers.forEach(result::add);
        return result;
    }
}
