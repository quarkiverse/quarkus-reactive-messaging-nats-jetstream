package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

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
import io.smallrye.mutiny.tuples.Tuple3;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.List;
import java.util.Map;

@JBossLog
public record PublisherAwareImpl(ExecutionHolder executionHolder, TracerFactory tracerFactory,
                                 PayloadMapper payloadMapper,
                                 Connection connection) implements PublisherAware, ContextAware, JetStreamAware {

    @Override
    public <T> Uni<Message<T>> publish(final Message<T> message, final String stream, final String subject) {
        return publish(message, stream, subject, new PublishListenerImpl());
    }

    @Override
    public <T> Uni<Message<T>> publish(final Message<T> message, final String stream, final String subject, final PublishListener listener) {
        return withContext(context -> context.executeBlocking(publish(stream, subject, message, listener)))
                .onFailure().invoke(listener::onError)
                .onFailure().transform(ClientException::new);
    }

    @Override
    public <T> Multi<Message<T>> publish(final Multi<Message<T>> messages, final String stream, final String subject, final PublishListener listener) {
        return withContext(context -> messages.onItem()
                .transformToUniAndMerge(message -> publish(stream, subject, message, listener)))
                .onFailure().invoke(listener::onError)
                .onFailure().transform(ClientException::new);
    }

    @Override
    public <T> Multi<Message<T>> publish(final Multi<Message<T>> messages, final String stream, final String subject) {
        return publish(messages, stream, subject, new PublishListenerImpl());
    }

    private <T> Uni<Message<T>> publish(final String stream, final String subject, final Message<T> message, final PublishListener listener) {
        return Uni.createFrom().item(Unchecked.supplier(() -> payloadMapper.map(message)))
                .onItem().transform(Unchecked.function(payload -> payloadMapper.map(payload)))
                .onItem().transform(Unchecked.function(payload -> addPublishMetadata(message, stream, subject, payload)))
                .onItem().transformToUni(msg -> tracerFactory.<T>create(TracerType.Publish).withTrace(msg, m -> m))
                .onItem().transformToUni(msg -> publish(stream, subject, msg))
                .onItem().invoke(tuple -> listener.onPublished(tuple.getItem1(), tuple.getItem3().getSeqno()))
                .onItem().transform(Tuple3::getItem2)
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(failure -> notAcknowledge(message, failure))
                .onFailure().transform(ClientException::new);
    }

    private <T> Uni<Tuple3<String, Message<T>, PublishAck>> publish(final String stream, final String subject, final Message<T> message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> message.getMetadata(PublishMessageMetadata.class).orElseThrow(() -> new RuntimeException("Message must have metadata"))))
                .onItem().transformToUni(metadata -> publish(subject, toJetStreamHeaders(metadata.payload().headers()), metadata.payload().data(), createPublishOptions(metadata.payload().id(), stream))
                        .onItem().transform(publishAck -> Tuple3.of(metadata.payload().id(), message, publishAck)));
    }

    private <T> Message<T> addPublishMetadata(final Message<T> message, final String stream, final String subject,
                                              final SerializedPayload<T> payload) {
        final var publishMetadata = PublishMessageMetadata.builder()
                .stream(stream)
                .subject(subject)
                .payload(payload)
                .build();
        final var metadata = message.getMetadata().without(PublishMessageMetadata.class);
        return message.withMetadata(metadata.with(publishMetadata));
    }

    private PublishOptions createPublishOptions(final String messageId, final String streamName) {
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

    private Uni<PublishAck> publish(final String subject, final Headers headers, final byte[] body, final PublishOptions options) {
        return jetStream()
                .onItem()
                .transformToUni(jetStream -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStream.publish(subject, headers, body, options))));
    }
}
