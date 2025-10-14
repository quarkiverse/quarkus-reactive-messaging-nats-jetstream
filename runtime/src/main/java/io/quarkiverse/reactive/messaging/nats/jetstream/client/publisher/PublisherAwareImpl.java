package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.nats.client.PublishOptions;
import io.nats.client.api.PublishAck;
import io.nats.client.impl.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SerializedPayload;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.JetStreamAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.context.ContextAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public record PublisherAwareImpl(ExecutionHolder executionHolder,
        TracerFactory tracerFactory,
        PayloadMapper payloadMapper,
        Connection connection) implements PublisherAware, ContextAware, JetStreamAware {

    @Override
    public <T> Uni<Message<T>> publish(final Message<T> message, final String stream, final String subject) {
        return publish(message, stream, subject, new PublishListenerImpl<>());
    }

    @Override
    public <T> Uni<Message<T>> publish(final Message<T> message, final String stream, final String subject,
            final PublishListener<T> listener) {
        return withContext(context -> context.executeBlocking(publishInternal(message, stream, subject, listener)))
                .onFailure().invoke(listener::onError)
                .onFailure().transform(ClientException::new);
    }

    @Override
    public <T> Multi<Message<T>> publish(final Multi<Message<T>> messages, final String stream, final String subject,
            final PublishListener<T> listener) {
        return withContext(context -> messages.onItem()
                .transformToUniAndMerge(message -> publishInternal(message, stream, subject, listener)))
                .onFailure().invoke(listener::onError)
                .onFailure().transform(ClientException::new);
    }

    @Override
    public <T> Multi<Message<T>> publish(final Multi<Message<T>> messages, final String stream, final String subject) {
        return publish(messages, stream, subject, new PublishListenerImpl<>());
    }

    private <T> Uni<Message<T>> publishInternal(final Message<T> message, final String stream, final String subject,
            final PublishListener<T> listener) {
        return Uni.createFrom().item(Unchecked.supplier(() -> payloadMapper.map(message)))
                .onItem().transform(Unchecked.function(payload -> payloadMapper.map(payload)))
                .onItem().transform(Unchecked.function(payload -> withMetadata(message, stream, subject, payload)))
                .onItem().transformToUni(this::withTrace)
                .onItem().transformToUni(tuple -> jetStream()
                        .onItem().transformToUni(jetStream -> Uni.createFrom().item(Unchecked.supplier(() -> jetStream.publish(
                                tuple.getItem2().subject(),
                                toJetStreamHeaders(tuple.getItem2().payload().headers()),
                                tuple.getItem2().payload().data(),
                                publishOptions(tuple.getItem2().payload().id(), tuple.getItem2().stream())))))
                        .onItem().transform(publishAck -> Tuple2.of(tuple.getItem1(), publishAck)))
                .onItem().transform(this::withSequence)
                .onItem().invoke(listener::onPublished)
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(failure -> notAcknowledge(message, failure))
                .onFailure().transform(ClientException::new);
    }

    private <T> Tuple2<Message<T>, PublishMessageMetadata> withMetadata(final Message<T> message, final String stream,
            final String subject,
            final SerializedPayload<T> payload) {
        final var publishMetadata = PublishMessageMetadata.builder()
                .stream(stream)
                .subject(subject)
                .payload(payload)
                .build();
        final var metadata = message.getMetadata().without(PublishMessageMetadata.class);
        return Tuple2.of(message.withMetadata(metadata.with(publishMetadata)), publishMetadata);
    }

    private <T> Uni<Tuple2<Message<T>, PublishMessageMetadata>> withTrace(Tuple2<Message<T>, PublishMessageMetadata> tuple) {
        return tracerFactory.<T> create(TracerType.Publish).withTrace(tuple.getItem1(), m -> m)
                .onItem().transform(message -> Tuple2.of(message, tuple.getItem2()));
    }

    private <T> Message<T> withSequence(final Tuple2<Message<T>, PublishAck> tuple) {
        final var publishMetadata = tuple.getItem1().getMetadata(PublishMessageMetadata.class)
                .orElseThrow(() -> new IllegalStateException("No metadata found"));
        final var metadata = tuple.getItem1().getMetadata().without(PublishMessageMetadata.class);
        return tuple.getItem1().withMetadata(metadata.with(PublishMessageMetadata.builder()
                .stream(publishMetadata.stream())
                .subject(publishMetadata.subject())
                .payload(publishMetadata.payload())
                .sequence(tuple.getItem2().getSeqno())
                .build()));
    }

    private PublishOptions publishOptions(final String messageId, final String streamName) {
        return PublishOptions.builder()
                .messageId(messageId)
                .expectedStream(streamName)
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

    private Headers toJetStreamHeaders(final Map<String, List<String>> headers) {
        final var result = new Headers();
        headers.forEach(result::add);
        return result;
    }

    private Uni<PublishAck> publish(final String subject, final Headers headers, final byte[] body,
            final PublishOptions options) {
        return jetStream()
                .onItem()
                .transformToUni(jetStream -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStream.publish(subject, headers, body, options))));
    }
}
