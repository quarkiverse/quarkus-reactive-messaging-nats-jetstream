package io.quarkiverse.reactive.nats.jetstream;

import io.quarkiverse.reactive.nats.jetstream.connection.NativeConnection;
import io.quarkiverse.reactive.nats.jetstream.message.Headers;
import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.quarkiverse.reactive.nats.jetstream.message.PublishMetadata;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.Context;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.UUID;

public record VertxClient(ClientConfiguration configuration, NativeConnection connection,
                          Context context, Collection<ClientListener> listeners) implements Client {

    @Override
    public @NonNull Uni<Message> publish(@NonNull Message message, @NonNull String stream, @NonNull String subject) {
        return Uni.createFrom().item(Unchecked.supplier(() -> withMetadata(message, stream, subject)))
                .chain(this::withTrace)
                .onItem().transformToUni(tuple -> jetStream(connection)
                        .onItem().transformToUni(jetStream -> Uni.createFrom().item(Unchecked.supplier(() -> jetStream.publish(
                                tuple.getItem2().subject(),
                                toJetStreamHeaders(tuple.getItem2().headers()),
                                tuple.getItem2().payload().data(),
                                publishOptions(tuple.getItem2().messageId(), tuple.getItem2().stream())))))
                        .onItem().transform(publishAck -> Tuple2.of(tuple.getItem1(), publishAck)))
                .onItem().transform(this::withSequence)
                .onItem().invoke(listener::onPublished)
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(failure -> notAcknowledge(message, failure))
                .onFailure().transform(ClientException::new);
    }

    @Override
    public @NonNull Multi<Message> publish(@NonNull Multi<Message> messages, @NonNull String stream, @NonNull String subject) {
        return null;
    }

    private Tuple2<Message, PublishMetadata> withMetadata(final Message message, final String stream, final String subject) {
        final var publishMetadata = PublishMetadata.of(stream,
                        subject,
                        messageId(message),
                        headers(message),
                        configuration.acknowledgeTimeout().orElse(null),
                        configuration.backoff());
        final var metadata = message.getMetadata().without(PublishMetadata.class);
        return Tuple2.of(Message.of(message.withMetadata(metadata)), publishMetadata);
    }

    private String messageId(final org.eclipse.microprofile.reactive.messaging.Message<?> message) {
        return message.getMetadata().get(PublishMetadata.class)
                .map(PublishMetadata::messageId)
                .orElseGet(() -> UUID.randomUUID().toString());
    }

    private Headers headers(final org.eclipse.microprofile.reactive.messaging.Message<?> message) {
        return message.getMetadata().get(PublishMetadata.class)
                .map(PublishMetadata::headers)
                .orElseGet(Headers::of);
    }

    private Uni<Tuple2<Message, PublishMetadata>> withTrace(Tuple2<Message, PublishMetadata> tuple) {
        return tracerFactory.<T> create(TracerType.Publish).withTrace(tuple.getItem1(), m -> m)
                .onItem().transform(message -> Tuple2.of(message, tuple.getItem2()));
    }
}
