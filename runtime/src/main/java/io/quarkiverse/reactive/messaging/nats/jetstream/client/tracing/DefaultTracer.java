package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import static io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation.RECEIVE;
import static io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesExtractor.create;
import static io.smallrye.reactive.messaging.tracing.TracingUtils.getOpenTelemetry;

import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.opentelemetry.api.OpenTelemetry;
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
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.tracing.TracingUtils;

public class DefaultTracer<T> implements Tracer<T> {
    private final JetStreamBuildConfiguration configuration;
    private final PayloadMapper payloadMapper;
    private final OpenTelemetry openTelemetry;
    private final Instrumenter<PublishMessage<T>, Void> publisher;
    private final Instrumenter<SubscribeMessage<T>, Void> subscriber;
    private final Instrumenter<ResolvedMessage<T>, Void> resolver;

    public DefaultTracer(Instance<OpenTelemetry> openTelemetryInstance,
            JetStreamBuildConfiguration configuration,
            PayloadMapper payloadMapper) {
        this.configuration = configuration;
        this.payloadMapper = payloadMapper;
        this.openTelemetry = getOpenTelemetry(openTelemetryInstance);
        this.subscriber = subscriber(openTelemetry);
        this.publisher = publisher(openTelemetry);
        this.resolver = resolver(openTelemetry);
    }

    @Override
    public Uni<SubscribeMessage<T>> withTrace(Message<T> message, PublishConfiguration configuration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> createSubscribeMessage(message, configuration)))
                .onItem().transformToUni(this::traceOutgoing);
    }

    @Override
    public Uni<Message<T>> withTrace(PublishMessage<T> message) {
        if (configuration.trace()) {
            return Uni.createFrom().item(Unchecked.supplier(() -> {
                TracingUtils.traceIncoming(publisher, message, message);
                return message;
            }));
        }
        return Uni.createFrom().item(message);
    }

    @Override
    public Uni<Message<T>> withTrace(ResolvedMessage<T> message) {
        if (configuration.trace()) {
            return Uni.createFrom().item(Unchecked.supplier(() -> {
                TracingUtils.traceIncoming(resolver, message, message);
                return message;
            }));
        }
        return Uni.createFrom().item(message);
    }

    private Uni<SubscribeMessage<T>> traceOutgoing(final Tuple2<SubscribeMessage<T>, Message<T>> tuple) {
        if (configuration.trace()) {
            return Uni.createFrom().item(Unchecked.supplier(() -> {
                TracingUtils.traceOutgoing(subscriber, tuple.getItem2(), tuple.getItem1());
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

    private Tuple2<SubscribeMessage<T>, Message<T>> createSubscribeMessage(Message<T> message,
            PublishConfiguration configuration) {
        final var payload = payloadMapper.of(message.getPayload());
        return Tuple2.of(SubscribeMessage.of(message, payload, configuration), message);
    }
}
