package io.quarkiverse.reactive.messaging.nats.jetstream.client.io;

import static io.smallrye.reactive.messaging.tracing.TracingUtils.traceIncoming;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamIncomingMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamTrace;
import io.vertx.mutiny.core.Context;

@ApplicationScoped
public class MessageFactory {
    private final PayloadMapper payloadMapper;
    private final Instrumenter<JetStreamTrace, Void> instrumenter;

    @Inject
    public MessageFactory(PayloadMapper payloadMapper,
            JetStreamInstrumenter instrumenter) {
        this.payloadMapper = payloadMapper;
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
                    ? new JetStreamIncomingMessage<>(message, payloadMapper.toPayload(message, payloadType), context,
                            exponentialBackoff, ackTimeout)
                    : new JetStreamIncomingMessage<>(message,
                            payloadMapper.toPayload(message).orElseGet(() -> message.getData()),
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

}
