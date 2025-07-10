package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessage.DEFAULT_ACK_TIMEOUT;

import java.time.Duration;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.JetStreamSubscription;
import io.nats.client.PushSubscribeOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerType;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.Context;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class PushSubscription<T> extends AbstractConsumer implements Subscription<T> {
    private final String stream;
    private final String consumer;
    private final PushConsumerConfiguration configuration;
    private final Connection connection;
    private final MessageMapper messageMapper;
    private final TracerFactory tracerFactory;
    private final Context context;

    private volatile JetStreamSubscription subscription;
    private volatile Dispatcher dispatcher;

    PushSubscription(final Connection connection,
            final String stream,
            final String consumer,
            final PushConsumerConfiguration configuration,
            final MessageMapper messageMapper,
            final TracerFactory tracerFactory,
            final Context context) {
        this.connection = connection;
        this.stream = stream;
        this.consumer = consumer;
        this.configuration = configuration;
        this.messageMapper = messageMapper;
        this.tracerFactory = tracerFactory;
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Multi<Message<T>> subscribe() {
        final Class<T> payloadType = (Class<T>) configuration.consumerConfiguration().payloadType().orElse(null);
        final var tracer = tracerFactory.<T> create(TracerType.Subscribe);
        return Multi.createFrom().<io.nats.client.Message> emitter(emitter -> {
            try {
                final var jetStream = connection.jetStream();
                dispatcher = connection.createDispatcher();
                final var pushOptions = createPushSubscribeOptions();
                subscription = jetStream.subscribe(
                        null, dispatcher,
                        emitter::emit,
                        false,
                        pushOptions);
            } catch (Exception e) {
                log.errorf(
                        e,
                        "Failed subscribing to stream: %s, subjects: %s with message: %s",
                        stream,
                        configuration.consumerConfiguration().filterSubjects(),
                        e.getMessage());
                emitter.fail(e);
            }
        })
                .emitOn(context::runOnContext)
                .map(message -> transformMessage(message, payloadType, context,
                        configuration.consumerConfiguration().acknowledgeTimeout().orElse(DEFAULT_ACK_TIMEOUT)))
                .onItem().transformToUniAndMerge(message -> tracer.withTrace(message, msg -> msg));
    }

    @Override
    public void close() {
        try {
            if (subscription.isActive()) {
                subscription.drain(Duration.ofMillis(1000));
            }
        } catch (Throwable failure) {
            log.warnf(failure, "Interrupted while draining subscription: %s", failure.getMessage());
        }
        try {
            if (subscription != null && dispatcher != null && dispatcher.isActive()) {
                dispatcher.unsubscribe(subscription);
            }
        } catch (Throwable failure) {
            log.warnf(failure, "Failed to shutdown pull executor: %s", failure.getMessage());
        }
    }

    private Message<T> transformMessage(io.nats.client.Message message, Class<T> payloadType, Context context,
            Duration timeout) {
        return messageMapper.of(message, payloadType, context, timeout,
                configuration.consumerConfiguration().backoff().orElseGet(List::of));
    }

    private PushSubscribeOptions createPushSubscribeOptions() {
        var builder = PushSubscribeOptions.builder();
        builder = configuration.pushConfiguration().flatMap(PushConfiguration::ordered).map(builder::ordered).orElse(builder);
        builder = configuration.pushConfiguration().flatMap(PushConfiguration::deliverGroup).map(builder::deliverGroup)
                .orElse(builder);
        if (configuration.consumerConfiguration().durable()) {
            builder = builder.durable(consumer);
        }
        builder = builder.stream(stream);
        builder = builder.configuration(createConsumerConfiguration(consumer, configuration));
        return builder.build();
    }
}
