package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.TracingMetadata;

/**
 * Traces the processing of a message delivered by a subscription. The span is current while the application processes
 * the message and ends when the message is acknowledged, or with the reason as error when it is not acknowledged. The
 * context of the span that published the message is its parent and is also linked.
 */
final class ProcessTracer implements Tracer {
    private final OpenTelemetry openTelemetry;
    private final Instrumenter<Message<byte[]>, Void> instrumenter;
    private final TraceSupplier traceSupplier;

    ProcessTracer(OpenTelemetry openTelemetry, boolean tracePayload) {
        this.openTelemetry = openTelemetry;
        this.instrumenter = Instrumenters.create(openTelemetry, Operation.PROCESS, tracePayload);
        this.traceSupplier = new AttachContextTraceSupplier();
    }

    @Override
    public @NonNull Uni<Message<byte[]>> withTrace(@NonNull Message<byte[]> message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> trace(message)))
                .chain(traceSupplier::get);
    }

    private Message<byte[]> trace(Message<byte[]> message) {
        final var parentContext = Instrumenters.producerContext(openTelemetry, Operation.PROCESS, Context.current(), message);
        if (!instrumenter.shouldStart(parentContext, message)) {
            return message;
        }
        final var spanContext = instrumenter.start(parentContext, message);
        final var ended = new AtomicBoolean();
        final Consumer<Throwable> end = error -> {
            if (ended.compareAndSet(false, true)) {
                instrumenter.end(spanContext, message, null, error);
            }
        };
        return message.addMetadata(TracingMetadata.with(spanContext, parentContext))
                .withAckWithMetadata(metadata -> endWhenDone(message.ack(metadata), end, null))
                .withNackWithMetadata((reason, metadata) -> endWhenDone(message.nack(reason, metadata), end, reason));
    }

    private static CompletionStage<Void> endWhenDone(@Nullable CompletionStage<Void> stage, Consumer<Throwable> end,
            @Nullable Throwable reason) {
        if (stage == null) {
            end.accept(reason);
            return CompletableFuture.completedFuture(null);
        }
        return stage.whenComplete((ignored, failure) -> end.accept(reason != null ? reason : failure));
    }
}
