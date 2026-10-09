package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.tracing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.MessageConsumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.Readiness;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.TestSpanExporter;
import io.quarkus.test.QuarkusExtensionTest;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;

class TracingTest {
    private static final AttributeKey<String> MESSAGING_SYSTEM = AttributeKey.stringKey("messaging.system");
    private static final AttributeKey<String> MESSAGING_DESTINATION = AttributeKey.stringKey("messaging.destination.name");
    private static final AttributeKey<String> MESSAGING_MESSAGE_ID = AttributeKey.stringKey("messaging.message.id");
    private static final AttributeKey<String> NATS_STREAM = AttributeKey.stringKey("messaging.nats.stream.name");
    private static final AttributeKey<String> NATS_CONSUMER = AttributeKey.stringKey("messaging.nats.consumer.name");
    private static final AttributeKey<String> NATS_PAYLOAD = AttributeKey.stringKey("messaging.nats.message.payload");

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(Readiness.class, TestSpanExporter.class, Data.class, DataResource.class,
                            DataConsumingBean.class,
                            DataCollectorBean.class, MessageConsumer.class))
            .withConfigurationResource("application-tracing.properties");

    @Inject
    TestSpanExporter spanExporter;

    @BeforeEach
    void setup() {
        Readiness.awaitReady();
        RestAssured.defaultParser = Parser.JSON;
        spanExporter.reset();
    }

    /**
     * REST request -> publish 'data-tracing' -> DataConsumingBean processes it and publishes 'data-stream' ->
     * DataCollectorBean processes that. Every hop is one span of a single trace, following the messaging semantic
     * conventions.
     */
    @Test
    void tracing() {
        final var messageId = "c923ca9b-27ac-4dc3-ad61-8c6733f93b11";
        final var data = "N6cXzadfafM";

        RestAssured.given().pathParam("id", messageId).pathParam("data", data).post("/data/{id}/{data}").then().statusCode(204);

        final var spans = spanExporter.getFinishedSpanItems(5);
        assertThat(spans).extracting(SpanData::getTraceId).containsOnly(spans.getFirst().getTraceId());

        final var request = single(spans, SpanKind.SERVER, null);
        final var publishData = single(spans, SpanKind.PRODUCER, "data-tracing");
        final var processData = single(spans, SpanKind.CONSUMER, "data-tracing");
        final var publishCollected = single(spans, SpanKind.PRODUCER, "data-stream");
        final var processCollected = single(spans, SpanKind.CONSUMER, "data-stream");

        assertThat(publishData.getParentSpanId()).isEqualTo(request.getSpanId());
        assertThat(publishData.getName()).isEqualTo("data-tracing publish");
        assertThat(publishData.getAttributes().get(MESSAGING_MESSAGE_ID)).isEqualTo(messageId);

        // the producer is the parent of the process span and is also linked
        assertThat(processData.getParentSpanId()).isEqualTo(publishData.getSpanId());
        assertThat(processData.getName()).isEqualTo("data-tracing process");
        assertThat(processData.getLinks()).extracting(link -> link.getSpanContext().getSpanId())
                .containsExactly(publishData.getSpanId());
        assertThat(processData.getAttributes().get(NATS_CONSUMER)).isEqualTo("data-consumer");

        // published while processing, so the process span is current; it ends when the message is acknowledged
        assertThat(publishCollected.getParentSpanId()).isEqualTo(processData.getSpanId());
        assertThat(processData.getEndEpochNanos()).isGreaterThanOrEqualTo(publishCollected.getEndEpochNanos());

        assertThat(processCollected.getParentSpanId()).isEqualTo(publishCollected.getSpanId());
        assertThat(processCollected.getLinks()).extracting(LinkData::getSpanContext)
                .allSatisfy(link -> assertThat(link.getSpanId()).isEqualTo(publishCollected.getSpanId()));

        for (var span : List.of(publishData, processData, publishCollected, processCollected)) {
            assertThat(span.getAttributes().get(MESSAGING_SYSTEM)).isEqualTo("nats");
            assertThat(span.getAttributes().get(NATS_STREAM)).isEqualTo("test-tracing");
            // the payload is only recorded when quarkus.messaging.nats.trace-payload is enabled
            assertThat(span.getAttributes().get(NATS_PAYLOAD)).isNull();
        }
    }

    private static SpanData single(List<SpanData> spans, SpanKind kind, String destination) {
        return spans.stream()
                .filter(span -> span.getKind() == kind)
                .filter(span -> destination == null || destination.equals(span.getAttributes().get(MESSAGING_DESTINATION)))
                .reduce((first, second) -> {
                    throw new AssertionError("More than one " + kind + " span for " + destination + ": " + spans);
                })
                .orElseThrow(() -> new AssertionError("No " + kind + " span for " + destination + ": " + spans));
    }

}
