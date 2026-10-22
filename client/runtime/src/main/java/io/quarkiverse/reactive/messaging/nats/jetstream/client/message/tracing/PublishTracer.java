package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import java.util.function.Function;

import org.jspecify.annotations.NonNull;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.quarkus.opentelemetry.runtime.QuarkusContextStorage;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

final class PublishTracer implements Tracer {
    private final Instrumenter<Message<byte[]>, Void> instrumenter;

    PublishTracer(OpenTelemetry openTelemetry, boolean tracePayload) {
        this.instrumenter = Instrumenters.create(openTelemetry, Operation.PUBLISH, tracePayload);
    }

    @Override
    public @NonNull Uni<Message<byte[]>> withTrace(@NonNull Message<byte[]> message) {
        return withTrace(message, msg -> Uni.createFrom().item(msg));
    }

    /**
     * The span is started before the operation, so the trace context is injected into the headers it publishes, and
     * ended when the operation terminates, so it covers the publish and records its failure.
     */
    @Override
    public @NonNull Uni<Message<byte[]>> withTrace(@NonNull Message<byte[]> message,
            @NonNull Function<Message<byte[]>, Uni<Message<byte[]>>> operation) {
        return addTracingMetadata(message).chain(msg -> {
            final var parentContext = msg.getMetadata(TraceContextMetadata.class)
                    .map(TraceContextMetadata::current)
                    .orElse(Context.current());
            if (!instrumenter.shouldStart(parentContext, msg)) {
                return operation.apply(msg);
            }
            final var spanContext = instrumenter.start(parentContext, msg);
            return Uni.createFrom().deferred(() -> operation.apply(msg))
                    .onTermination().invoke((result, failure, cancelled) -> instrumenter.end(spanContext, msg, null, failure));
        });
    }

    /**
     * For outgoing messages, if the message doesn't already contain a {@link TraceContextMetadata}, it attaches one with
     * the current OpenTelemetry context, which is the parent of the publish span.
     */
    private Uni<Message<byte[]>> addTracingMetadata(final Message<byte[]> message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            if (message.getMetadata(TraceContextMetadata.class).isEmpty()) {
                var otelContext = QuarkusContextStorage.INSTANCE.current();
                return message.addMetadata(TraceContextMetadata.of(otelContext));
            }
            return message;
        }));
    }
}
