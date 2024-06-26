package io.quarkiverse.reactive.messaging.nats.jetstream.tracing;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesGetter;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingSpanNameExtractor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import static io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation.RECEIVE;
import static io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesExtractor.create;
import static io.smallrye.reactive.messaging.tracing.TracingUtils.getOpenTelemetry;

@ApplicationScoped
public class JetStreamInstrumenter {
    private final Instance<OpenTelemetry> openTelemetryInstance;

    @Inject
    public JetStreamInstrumenter(Instance<OpenTelemetry> openTelemetryInstance) {
        this.openTelemetryInstance = openTelemetryInstance;
    }

    public Instrumenter<JetStreamTrace, Void> publisher() {
        final var attributesExtractor = new JetStreamTraceAttributesExtractor();
        MessagingAttributesGetter<JetStreamTrace, Void> messagingAttributesGetter = attributesExtractor
                .getMessagingAttributesGetter();

        InstrumenterBuilder<JetStreamTrace, Void> builder = Instrumenter.builder(getOpenTelemetry(openTelemetryInstance),
                "io.smallrye.reactive.messaging.jetstream",
                MessagingSpanNameExtractor.create(messagingAttributesGetter, MessageOperation.PUBLISH));

        return builder.addAttributesExtractor(create(messagingAttributesGetter, MessageOperation.PUBLISH))
                .addAttributesExtractor(attributesExtractor)
                .buildProducerInstrumenter(JetStreamTraceTextMapSetter.INSTANCE);
    }

    public Instrumenter<JetStreamTrace, Void> receiver() {
        final var attributesExtractor = new JetStreamTraceAttributesExtractor();
        MessagingAttributesGetter<JetStreamTrace, Void> messagingAttributesGetter = attributesExtractor
                .getMessagingAttributesGetter();
        InstrumenterBuilder<JetStreamTrace, Void> builder = Instrumenter.builder(getOpenTelemetry(openTelemetryInstance),
                "io.smallrye.reactive.messaging.jetstream",
                MessagingSpanNameExtractor.create(messagingAttributesGetter, RECEIVE));

        return builder.addAttributesExtractor(attributesExtractor)
                .addAttributesExtractor(MessagingAttributesExtractor.create(messagingAttributesGetter, RECEIVE))
                .buildConsumerInstrumenter(JetStreamTraceTextMapGetter.INSTANCE);
    }

}
