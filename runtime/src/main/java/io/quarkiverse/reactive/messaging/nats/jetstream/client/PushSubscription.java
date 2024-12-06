package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.Dispatcher;
import io.nats.client.JetStreamSubscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushSubscribeOptionsFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.Tracer;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.Context;

public class PushSubscription<P> implements Subscription<P> {
    private static final Logger logger = Logger.getLogger(PushSubscription.class);

    private final Connection connection;
    private final PushConsumerConfiguration<P> consumerConfiguration;
    private final PushSubscribeOptionsFactory pushSubscribeOptionsFactory;
    private final io.nats.client.Connection natsConnection;
    private final MessageMapper messageMapper;

    private volatile JetStreamSubscription subscription;
    private volatile Dispatcher dispatcher;

    PushSubscription(final Connection connection,
            final PushConsumerConfiguration<P> consumerConfiguration,
            final io.nats.client.Connection natsConnection,
            final MessageMapper messageMapper) {
        this.connection = connection;
        this.consumerConfiguration = consumerConfiguration;
        this.pushSubscribeOptionsFactory = new PushSubscribeOptionsFactory();
        this.natsConnection = natsConnection;
        this.messageMapper = messageMapper;
    }

    @Override
    public Multi<Message<P>> subscribe(Tracer<P> tracer, Context context) {
        Class<P> payloadType = consumerConfiguration.consumerConfiguration().payloadType().orElse(null);
        final var subject = consumerConfiguration.subject();
        return Multi.createFrom().<io.nats.client.Message> emitter(emitter -> {
            try {
                final var jetStream = natsConnection.jetStream();
                dispatcher = natsConnection.createDispatcher();
                final var pushOptions = pushSubscribeOptionsFactory.create(consumerConfiguration);
                subscription = jetStream.subscribe(
                        subject, dispatcher,
                        emitter::emit,
                        false,
                        pushOptions);
            } catch (Exception e) {
                logger.errorf(
                        e,
                        "Failed subscribing to stream: %s, subject: %s with message: %s",
                        consumerConfiguration.consumerConfiguration().stream(),
                        subject,
                        e.getMessage());
                emitter.fail(e);
            }
        })
                .emitOn(context::runOnContext)
                .map(message -> messageMapper.of(
                        message,
                        payloadType,
                        context,
                        new ExponentialBackoff(
                                consumerConfiguration.consumerConfiguration().exponentialBackoff(),
                                consumerConfiguration.consumerConfiguration().exponentialBackoffMaxDuration()),
                        consumerConfiguration.consumerConfiguration().ackTimeout()))
                .onItem().transformToUniAndMerge(tracer::withTrace);
    }

    @Override
    public void onEvent(ConnectionEvent event, String message) {

    }

    @Override
    public void close() {
        try {
            if (subscription.isActive()) {
                subscription.drain(Duration.ofMillis(1000));
            }
        } catch (Throwable failure) {
            logger.warnf(failure, "Interrupted while draining subscription: %s", failure.getMessage());
        }
        try {
            if (subscription != null && dispatcher != null && dispatcher.isActive()) {
                dispatcher.unsubscribe(subscription);
            }
        } catch (Throwable failure) {
            logger.warnf(failure, "Failed to shutdown pull executor: %s", failure.getMessage());
        }
        connection.removeListener(this);
    }
}
