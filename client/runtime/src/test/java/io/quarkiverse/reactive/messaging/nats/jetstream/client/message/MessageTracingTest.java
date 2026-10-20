package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.nats.client.impl.NatsMessage;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.OpenTelemetryTracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.Operation;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;

class MessageTracingTest {
    private static final AttributeKey<String> MESSAGING_SYSTEM = AttributeKey.stringKey("messaging.system");
    private static final AttributeKey<String> MESSAGING_DESTINATION = AttributeKey.stringKey("messaging.destination.name");
    private static final AttributeKey<String> MESSAGING_MESSAGE_ID = AttributeKey.stringKey("messaging.message.id");
    private static final AttributeKey<Long> MESSAGING_BODY_SIZE = AttributeKey.longKey("messaging.message.body.size");
    private static final AttributeKey<String> NATS_STREAM = AttributeKey.stringKey("messaging.nats.stream.name");
    private static final AttributeKey<String> NATS_CONSUMER = AttributeKey.stringKey("messaging.nats.consumer.name");
    private static final AttributeKey<String> NATS_PAYLOAD = AttributeKey.stringKey("messaging.nats.message.payload");
    private static final String PRODUCER_TRACE_ID = "0af7651916cd43dd8448eb211c80319c";
    private static final String PRODUCER_SPAN_ID = "b7ad6b7169203331";

    private final InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
    private final OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(spanExporter)).build())
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build();
    private final OpenTelemetryTracerFactory tracerFactory = new OpenTelemetryTracerFactory(instance(openTelemetry));

    @AfterEach
    void tearDown() {
        openTelemetry.close();
    }

    @Test
    void publishSpanCoversThePublish() {
        final var tracer = tracerFactory.create(Operation.PUBLISH);

        tracer.withTrace(publishMessage(), msg -> {
            // the span is still open while publishing and its context is already in the published headers
            assertThat(spanExporter.getFinishedSpanItems()).isEmpty();
            assertThat(msg.getMetadata(PublishHeaders.class).orElseThrow()).containsKey("traceparent");
            return Uni.createFrom().item(msg);
        }).subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem();

        assertThat(singleSpan()).satisfies(span -> {
            assertThat(span.getKind()).isEqualTo(SpanKind.PRODUCER);
            assertThat(span.getName()).isEqualTo("orders publish");
            assertThat(span.getStatus().getStatusCode()).isEqualTo(StatusCode.UNSET);
            assertThat(span.getAttributes().get(MESSAGING_SYSTEM)).isEqualTo("nats");
            assertThat(span.getAttributes().get(MESSAGING_DESTINATION)).isEqualTo("orders");
            assertThat(span.getAttributes().get(MESSAGING_MESSAGE_ID)).isEqualTo("message-id");
            assertThat(span.getAttributes().get(MESSAGING_BODY_SIZE)).isEqualTo(7L);
            assertThat(span.getAttributes().get(NATS_STREAM)).isEqualTo("orders-stream");
            assertThat(span.getAttributes().get(NATS_PAYLOAD)).isNull();
        });
    }

    @Test
    void publishSpanRecordsPublishFailure() {
        final var tracer = tracerFactory.create(Operation.PUBLISH);

        tracer.withTrace(publishMessage(), msg -> Uni.createFrom().failure(new IOException("No Responders")))
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitFailure()
                .assertFailedWith(IOException.class, "No Responders");

        assertThat(singleSpan()).satisfies(span -> {
            assertThat(span.getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);
            assertThat(span.getEvents()).anySatisfy(event -> assertThat(event.getName()).isEqualTo("exception"));
        });
    }

    @Test
    void payloadIsOnlyRecordedWhenEnabled() {
        final var tracer = new OpenTelemetryTracerFactory(instance(openTelemetry), true).create(Operation.PUBLISH);

        tracer.withTrace(publishMessage()).subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem();

        assertThat(singleSpan().getAttributes().get(NATS_PAYLOAD)).isEqualTo("payload");
    }

    @Test
    void receiveSpanIsClientSpanLinkedToProducer() {
        final var tracer = tracerFactory.create(Operation.RECEIVE);

        tracer.withTrace(receivedMessage()).subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem();

        assertThat(singleSpan()).satisfies(span -> {
            assertThat(span.getKind()).isEqualTo(SpanKind.CLIENT);
            assertThat(span.getName()).isEqualTo("orders receive");
            assertThat(span.getTraceId()).isEqualTo(PRODUCER_TRACE_ID);
            assertThat(span.getParentSpanId()).isEqualTo(PRODUCER_SPAN_ID);
            assertThat(span.getLinks()).singleElement()
                    .satisfies(link -> assertThat(link.getSpanContext().getSpanId()).isEqualTo(PRODUCER_SPAN_ID));
            // published by another client: no stream header, only the JetStream delivery metadata names the stream
            assertThat(span.getAttributes().get(NATS_STREAM)).isEqualTo("orders-stream");
            assertThat(span.getAttributes().get(NATS_CONSUMER)).isEqualTo("orders-consumer");
        });
    }

    @Test
    void processSpanEndsWhenMessageIsAcknowledged() {
        final var tracer = tracerFactory.create(Operation.PROCESS);

        final var traced = tracer.withTrace(receivedMessage()).subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem().getItem();
        assertThat(spanExporter.getFinishedSpanItems()).isEmpty();

        traced.ack().toCompletableFuture().join();
        // acknowledging twice must not end the span twice
        traced.ack().toCompletableFuture().join();

        assertThat(singleSpan()).satisfies(span -> {
            assertThat(span.getKind()).isEqualTo(SpanKind.CONSUMER);
            assertThat(span.getName()).isEqualTo("orders process");
            assertThat(span.getStatus().getStatusCode()).isEqualTo(StatusCode.UNSET);
            assertThat(span.getParentSpanId()).isEqualTo(PRODUCER_SPAN_ID);
            assertThat(span.getLinks()).singleElement()
                    .satisfies(link -> assertThat(link.getSpanContext().getSpanId()).isEqualTo(PRODUCER_SPAN_ID));
        });
    }

    @Test
    void processSpanRecordsNackReasonAsError() {
        final var tracer = tracerFactory.create(Operation.PROCESS);

        final var traced = tracer.withTrace(receivedMessage()).subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem().getItem();
        traced.nack(new IllegalStateException("processing failed")).toCompletableFuture().join();

        assertThat(singleSpan()).satisfies(span -> {
            assertThat(span.getStatus().getStatusCode()).isEqualTo(StatusCode.ERROR);
            assertThat(span.getEvents()).anySatisfy(event -> assertThat(event.getName()).isEqualTo("exception"));
        });
    }

    private SpanData singleSpan() {
        assertThat(spanExporter.getFinishedSpanItems()).hasSize(1);
        return spanExporter.getFinishedSpanItems().getFirst();
    }

    private static Message<byte[]> publishMessage() {
        final var headers = PublishHeaders.of("message-id");
        headers.setStream("orders-stream");
        headers.setSubject("orders");
        return Message.of("payload".getBytes(StandardCharsets.UTF_8), Metadata.of(headers));
    }

    /**
     * A message as received from JetStream, published by another client in the trace
     * {@value PRODUCER_TRACE_ID}: it carries the producer's trace context but no stream header.
     */
    private static Message<byte[]> receivedMessage() {
        final var natsHeaders = new io.nats.client.impl.Headers()
                .add("traceparent", "00-" + PRODUCER_TRACE_ID + "-" + PRODUCER_SPAN_ID + "-01");
        final var headers = MessageHeaders.of(NativeMessage.of(NatsMessage.builder()
                .subject("orders")
                .headers(natsHeaders)
                .data("payload", StandardCharsets.UTF_8)
                .build()));
        final var metadata = MessageMetadataImpl.builder()
                .stream("orders-stream")
                .consumer("orders-consumer")
                .deliveredCount(1)
                .streamSequence(1)
                .consumerSequence(1)
                .timestamp(ZonedDateTime.now())
                .build();
        return Message.of("payload".getBytes(StandardCharsets.UTF_8), Metadata.of(headers, metadata));
    }

    @SuppressWarnings("unchecked")
    private static Instance<OpenTelemetry> instance(OpenTelemetry openTelemetry) {
        return (Instance<OpenTelemetry>) Proxy.newProxyInstance(Instance.class.getClassLoader(),
                new Class<?>[] { Instance.class },
                (proxy, method, args) -> switch (method.getName()) {
                    case "isResolvable" -> true;
                    case "get" -> openTelemetry;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }
}
