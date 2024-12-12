package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import static io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation.RECEIVE;
import static io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesExtractor.create;
import static io.smallrye.reactive.messaging.tracing.TracingUtils.getOpenTelemetry;

import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesGetter;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingSpanNameExtractor;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamBuildConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.ResolvedMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PublishConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkus.opentelemetry.runtime.QuarkusContextStorage;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;
import io.smallrye.reactive.messaging.tracing.TracingUtils;

public class DefaultTracer<T> implements Tracer<T> {
    private final JetStreamBuildConfiguration configuration;
    private final PayloadMapper payloadMapper;
    private final Instrumenter<PublishMessage<T>, Void> publisher;
    private final Instrumenter<SubscribeMessage<T>, Void> subscriber;
    private final Instrumenter<ResolvedMessage<T>, Void> resolver;
    private final boolean connector;

    public DefaultTracer(Instance<OpenTelemetry> openTelemetryInstance,
            JetStreamBuildConfiguration configuration,
            PayloadMapper payloadMapper, boolean connector) {
        this.configuration = configuration;
        this.payloadMapper = payloadMapper;
        final var openTelemetry = getOpenTelemetry(openTelemetryInstance);
        this.subscriber = subscriber(openTelemetry);
        this.publisher = publisher(openTelemetry);
        this.resolver = resolver(openTelemetry);
        this.connector = connector;
    }

    @Override
    public Uni<SubscribeMessage<T>> withTrace(Message<T> message, PublishConfiguration configuration) {
        return addTracingMetadata(message)
                .onItem().transform(Unchecked.function(m -> createSubscribeMessage(m, configuration)))
                .onItem().transformToUni(this::traceOutgoing);
    }

    @Override
    public Uni<Message<T>> withTrace(PublishMessage<T> message) {
        if (configuration.trace()) {
            return Uni.createFrom().item(Unchecked.supplier(() -> {
                TracingUtils.traceIncoming(publisher, message, message);
                return attachContext(message);
            }));
        }
        return Uni.createFrom().item(message);
    }

    @Override
    public Uni<Message<T>> withTrace(ResolvedMessage<T> message) {
        if (configuration.trace()) {
            return Uni.createFrom().item(Unchecked.supplier(() -> {
                TracingUtils.traceIncoming(resolver, message, message);
                return attachContext(message);
            }));
        }
        return Uni.createFrom().item(message);
    }

    private Uni<SubscribeMessage<T>> traceOutgoing(final Tuple2<SubscribeMessage<T>, Message<T>> tuple) {
        if (configuration.trace()) {
            return Uni.createFrom().item(Unchecked.supplier(() -> {
                TracingUtils.traceOutgoing(subscriber, tuple.getItem1(), tuple.getItem1());
                return tuple.getItem1();
            }));
        } else {
            return Uni.createFrom().item(tuple.getItem1());
        }
    }

    private Instrumenter<SubscribeMessage<T>, Void> subscriber(OpenTelemetry openTelemetry) {
        final var attributesExtractor = new SubscribeMessageAttributesExtractor<T>();
        MessagingAttributesGetter<SubscribeMessage<T>, Void> messagingAttributesGetter = attributesExtractor
                .getMessagingAttributesGetter();
        InstrumenterBuilder<SubscribeMessage<T>, Void> builder = Instrumenter.builder(openTelemetry,
                "io.smallrye.reactive.messaging.jetstream",
                MessagingSpanNameExtractor.create(messagingAttributesGetter, MessageOperation.SEND));
        return builder.addAttributesExtractor(create(messagingAttributesGetter, MessageOperation.SEND))
                .addAttributesExtractor(attributesExtractor)
                .buildProducerInstrumenter(new SubscribeMessageTextMapSetter<>());
    }

    private Instrumenter<PublishMessage<T>, Void> publisher(OpenTelemetry openTelemetry) {
        final var attributesExtractor = new PublishMessageAttributesExtractor<T>();
        MessagingAttributesGetter<PublishMessage<T>, Void> messagingAttributesGetter = attributesExtractor
                .getMessagingAttributesGetter();
        InstrumenterBuilder<PublishMessage<T>, Void> builder = Instrumenter.builder(openTelemetry,
                "io.smallrye.reactive.messaging.jetstream",
                MessagingSpanNameExtractor.create(messagingAttributesGetter, RECEIVE));

        return builder.addAttributesExtractor(attributesExtractor)
                .addAttributesExtractor(MessagingAttributesExtractor.create(messagingAttributesGetter, RECEIVE))
                .buildConsumerInstrumenter(new PublishMessageTextMapGetter<>());
    }

    private Instrumenter<ResolvedMessage<T>, Void> resolver(OpenTelemetry openTelemetry) {
        final var attributesExtractor = new ResolvedMessageAttributesExtractor<T>();
        MessagingAttributesGetter<ResolvedMessage<T>, Void> messagingAttributesGetter = attributesExtractor
                .getMessagingAttributesGetter();
        InstrumenterBuilder<ResolvedMessage<T>, Void> builder = Instrumenter.builder(openTelemetry,
                "io.smallrye.reactive.messaging.jetstream",
                MessagingSpanNameExtractor.create(messagingAttributesGetter, RECEIVE));

        return builder.addAttributesExtractor(attributesExtractor)
                .addAttributesExtractor(MessagingAttributesExtractor.create(messagingAttributesGetter, RECEIVE))
                .buildConsumerInstrumenter(new ResolvedMessageTextMapGetter<>());
    }

    private Tuple2<SubscribeMessage<T>, Message<T>> createSubscribeMessage(final Message<T> message,
            final PublishConfiguration configuration) {
        final var payload = payloadMapper.of(message.getPayload());
        return Tuple2.of(SubscribeMessage.of(message, payload, configuration), message);
    }

    /**
     * For incoming messages, it fetches OpenTelemetry context from the message and attaches to the duplicated context of the
     * message.
     * Consumer methods will be called on this duplicated context, so the OpenTelemetry context associated with the incoming
     * message
     * will be propagated.
     */
    @SuppressWarnings("resource")
    private Message<T> attachContext(Message<T> message) {
        if (connector) {
            var messageContext = message.getMetadata(LocalContextMetadata.class)
                    .map(LocalContextMetadata::context)
                    .orElse(null);
            var otelContext = TracingMetadata.fromMessage(message)
                    .map(TracingMetadata::getCurrentContext)
                    .orElse(Context.current());
            if (messageContext != null && otelContext != null) {
                QuarkusContextStorage.INSTANCE.attach(messageContext, otelContext);
            }
        }
        return message;
    }

    /**
     * For outgoing messages, if the message doesn't already contain a tracing metadata, it attaches one with the current
     * OpenTelemetry context.
     * Reactive messaging outbound connectors, if tracing is supported, will use that context as parent span to trace outbound
     * message transmission.
     */
    private Uni<Message<T>> addTracingMetadata(final Message<T> message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            if (configuration.trace() && message.getMetadata(TracingMetadata.class).isEmpty()) {
                var otelContext = QuarkusContextStorage.INSTANCE.current();
                return message.addMetadata(TracingMetadata.withCurrent(otelContext));
            }
            return message;
        }));
    }
}
