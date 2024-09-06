package io.quarkiverse.reactive.messaging.nats.jetstream.client.vertx;

import java.time.Duration;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.Dispatcher;
import io.nats.client.JetStreamSubscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.MessageSubscribeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushSubscribeOptionsFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;

public class PushSubscribeMessageConnection<K> extends MessageConnection implements MessageSubscribeConnection {
    private static final Logger logger = Logger.getLogger(PushSubscribeMessageConnection.class);

    private final PushConsumerConfiguration<K> consumerConfiguration;
    private final PushSubscribeOptionsFactory optionsFactory;
    private volatile JetStreamSubscription subscription;
    private volatile Dispatcher dispatcher;

    public PushSubscribeMessageConnection(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener,
            Context context,
            JetStreamInstrumenter instrumenter,
            PushConsumerConfiguration<K> consumerConfiguration,
            MessageFactory messageFactory,
            PushSubscribeOptionsFactory optionsFactory) throws ConnectionException {
        super(connectionConfiguration, connectionListener, messageFactory, context, instrumenter);
        this.consumerConfiguration = consumerConfiguration;
        this.optionsFactory = optionsFactory;
    }

    @Override
    public Multi<Message<?>> subscribe() {
        boolean traceEnabled = consumerConfiguration.consumerConfiguration().traceEnabled();
        Class<?> payloadType = consumerConfiguration.consumerConfiguration().payloadType().orElse(null);
        final var subject = consumerConfiguration.subject();
        return Multi.createFrom().<io.nats.client.Message> emitter(emitter -> {
            try {
                final var jetStream = connection.jetStream();
                dispatcher = connection.createDispatcher();
                final var pushOptions = optionsFactory.create(consumerConfiguration);
                subscription = jetStream.subscribe(
                        subject, dispatcher,
                        emitter::emit,
                        false,
                        pushOptions);
            } catch (Throwable e) {
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
                .map(message -> messageFactory.create(
                        message,
                        traceEnabled,
                        payloadType,
                        context,
                        new ExponentialBackoff(
                                consumerConfiguration.consumerConfiguration().exponentialBackoff(),
                                consumerConfiguration.consumerConfiguration().exponentialBackoffMaxDuration()),
                        consumerConfiguration.consumerConfiguration().ackTimeout()));
    }

    @Override
    public Uni<Void> flush(Duration duration) {
        return super.flush(duration)
                .emitOn(context::runOnContext);
    }

    @Override
    public void close() throws Exception {
        try {
            if (subscription.isActive()) {
                subscription.drain(Duration.ofMillis(1000));
            }
        } catch (InterruptedException | IllegalStateException e) {
            logger.warnf("Interrupted while draining subscription");
        }
        try {
            if (subscription != null && dispatcher != null && dispatcher.isActive()) {
                dispatcher.unsubscribe(subscription);
            }
        } catch (Exception e) {
            logger.errorf(e, "Failed to shutdown pull executor");
        }
        super.close();
    }
}
