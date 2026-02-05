package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import static java.util.Comparator.comparingLong;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.quarkus.arc.Unremovable;

@Unremovable
@ApplicationScoped
public class TestSpanExporter implements SpanExporter {
    private final InMemorySpanExporter spanExporter;

    public TestSpanExporter() {
        this.spanExporter = InMemorySpanExporter.create();
    }

    /**
     * Careful when retrieving the list of finished spans. There is a chance when the response is already sent to the
     * client and Vert.x still writing the end of the spans. This means that a response is available to assert from the
     * test side but not all spans may be available yet. For this reason, this method requires the number of expected
     * spans.
     */
    public List<SpanData> getFinishedSpanItems(int spanCount) {
        assertSpanCount(spanCount);
        return spanExporter.getFinishedSpanItems().stream().sorted(comparingLong(SpanData::getStartEpochNanos).reversed())
                .collect(Collectors.toList());
    }

    public void assertSpanCount(int spanCount) {
        await().atMost(30, SECONDS).untilAsserted(() -> assertEquals(spanCount, spanExporter.getFinishedSpanItems().size()));
    }

    public void reset() {
        spanExporter.reset();
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        return spanExporter.export(spans);
    }

    @Override
    public CompletableResultCode flush() {
        return spanExporter.flush();
    }

    @Override
    public CompletableResultCode shutdown() {
        return spanExporter.shutdown();
    }
}
