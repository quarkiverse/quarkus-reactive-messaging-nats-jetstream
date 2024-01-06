package io.quarkiverse.reactive.messsaging.nats.jetstream.tracing;

import static io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation.RECEIVE;
import static io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesExtractor.create;

import jakarta.enterprise.context.ApplicationScoped;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesGetter;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingSpanNameExtractor;

@ApplicationScoped
public class JetStreamInstrumenter {

    public Instrumenter<JetStreamTrace, Void> publisher() {
        final var attributesExtractor = new JetStreamTraceAttributesExtractor();
        MessagingAttributesGetter<JetStreamTrace, Void> messagingAttributesGetter = attributesExtractor
                .getMessagingAttributesGetter();

        InstrumenterBuilder<JetStreamTrace, Void> builder = Instrumenter.builder(GlobalOpenTelemetry.get(),
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
        InstrumenterBuilder<JetStreamTrace, Void> builder = Instrumenter.builder(GlobalOpenTelemetry.get(),
                "io.smallrye.reactive.messaging.jetstream",
                MessagingSpanNameExtractor.create(messagingAttributesGetter, RECEIVE));

        return builder.addAttributesExtractor(attributesExtractor)
                .addAttributesExtractor(MessagingAttributesExtractor.create(messagingAttributesGetter, RECEIVE))
                .buildConsumerInstrumenter(JetStreamTraceTextMapGetter.INSTANCE);
    }

}
