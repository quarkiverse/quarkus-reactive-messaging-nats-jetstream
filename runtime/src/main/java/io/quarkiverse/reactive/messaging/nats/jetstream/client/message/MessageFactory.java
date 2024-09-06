package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import static io.smallrye.reactive.messaging.tracing.TracingUtils.traceIncoming;
import static io.smallrye.reactive.messaging.tracing.TracingUtils.traceOutgoing;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.api.MessageInfo;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamIncomingMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamOutgoingMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamTrace;
import io.vertx.mutiny.core.Context;

@ApplicationScoped
public class MessageFactory {
    private final static Logger logger = Logger.getLogger(MessageFactory.class);

    public static final String MESSAGE_TYPE_HEADER = "message.type";

    private final ObjectMapper objectMapper;
    private final Instrumenter<JetStreamTrace, Void> instrumenter;

    @Inject
    public MessageFactory(ObjectMapper objectMapper,
            JetStreamInstrumenter instrumenter) {
        this.objectMapper = objectMapper;
        this.instrumenter = instrumenter.receiver();
    }

    @SuppressWarnings("unchecked")
    public <T> org.eclipse.microprofile.reactive.messaging.Message<T> create(io.nats.client.Message message,
            boolean tracingEnabled,
            Class<?> payloadType,
            Context context,
            ExponentialBackoff exponentialBackoff,
            Duration ackTimeout) {
        try {
            final var incomingMessage = payloadType != null
                    ? new JetStreamIncomingMessage<>(message, toPayload(message, payloadType), context,
                            exponentialBackoff, ackTimeout)
                    : new JetStreamIncomingMessage<>(message,
                            toPayload(message).orElseGet(() -> message.getData()),
                            context,
                            exponentialBackoff, ackTimeout);
            if (tracingEnabled) {
                return (Message<T>) traceIncoming(instrumenter, incomingMessage, JetStreamTrace.trace(incomingMessage));
            } else {
                return (Message<T>) incomingMessage;
            }
        } catch (ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> MessagePayload create(org.eclipse.microprofile.reactive.messaging.Message<T> message,
            boolean tracingEnabled,
            String stream,
            String subject) {
        final var metadata = message.getMetadata(JetStreamOutgoingMessageMetadata.class);
        final var messageId = metadata.map(JetStreamOutgoingMessageMetadata::messageId)
                .orElseGet(() -> UUID.randomUUID().toString());
        final var payload = toByteArray(message.getPayload());
        final var messageSubject = metadata.flatMap(JetStreamOutgoingMessageMetadata::subtopic)
                .map(subtopic -> subject + "." + subtopic).orElse(subject);
        final var headers = new HashMap<String, List<String>>();
        metadata.ifPresent(m -> headers.putAll(m.headers()));
        if (message.getPayload() != null) {
            headers.putIfAbsent(MESSAGE_TYPE_HEADER, List.of(message.getPayload().getClass().getTypeName()));
        }

        if (tracingEnabled) {
            // Create a new span for the outbound message and record updated tracing information in
            // the headers; this has to be done before we build the properties below
            traceOutgoing(instrumenter, message,
                    new JetStreamTrace(stream, messageSubject, messageId, headers,
                            new String(payload)));
        }
        return new MessagePayload(messageId, stream, messageSubject, payload, headers);
    }

    /**
     * Returns a byte array containing the supplied payload.
     *
     * @param payload the payload
     * @return a byte array encapsulation of the payload
     */
    public byte[] toByteArray(final Object payload) {
        try {
            if (payload == null) {
                return new byte[0];
            } else if (payload instanceof byte[]) {
                final var byteArray = (byte[]) payload;
                return byteArray;
            } else {
                return objectMapper.writeValueAsBytes(payload);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<? super T> toPayload(io.nats.client.Message message) {
        return Optional.ofNullable(message).flatMap(m -> Optional.ofNullable(m.getHeaders()))
                .flatMap(headers -> Optional.ofNullable(headers.getFirst(MESSAGE_TYPE_HEADER)))
                .map(MessageFactory::loadClass)
                .map(type -> decode(message.getData(), type));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> toPayload(MessageInfo message) {
        logger.infof("Getting payload from message info: %s", message);
        return Optional.ofNullable(message).flatMap(m -> Optional.ofNullable(m.getHeaders()))
                .flatMap(headers -> Optional.ofNullable(headers.getFirst(MESSAGE_TYPE_HEADER)))
                .map(MessageFactory::loadClass)
                .map(type -> (T) decode(message.getData(), type));
    }

    public <T> T toPayload(io.nats.client.Message message, Class<T> payLoadType) {
        return decode(message.getData(), payLoadType);
    }

    public <T> T decode(byte[] data, Class<T> type) {
        try {
            return objectMapper.readValue(data, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(String type) {
        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return (Class<T>) classLoader.loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
