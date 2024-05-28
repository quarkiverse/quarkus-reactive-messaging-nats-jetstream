package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;

import io.nats.client.Dispatcher;
import io.nats.client.JetStreamSubscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionEvent;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushSubscribeOptionsFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.io.MessageFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.smallrye.mutiny.Multi;

public class MessagePushPublisherProcessor implements MessagePublisherProcessor {
    private final static Logger logger = Logger.getLogger(MessagePushPublisherProcessor.class);

    private final MessagePushPublisherConfiguration<?> configuration;
    private final JetStreamClient jetStreamClient;
    private final AtomicReference<Status> status;
    private final PushSubscribeOptionsFactory optionsFactory;
    private final MessageFactory messageFactory;

    private volatile JetStreamSubscription subscription;
    private volatile Dispatcher dispatcher;

    public MessagePushPublisherProcessor(final JetStreamClient jetStreamClient,
            final MessagePushPublisherConfiguration<?> configuration,
            final MessageFactory messageFactory) {
        this.configuration = configuration;
        this.jetStreamClient = jetStreamClient;
        this.messageFactory = messageFactory;
        this.status = new AtomicReference<>(new Status(false, "Not connected", ConnectionEvent.Closed));
        this.optionsFactory = new PushSubscribeOptionsFactory();
    }

    @Override
    public Multi<org.eclipse.microprofile.reactive.messaging.Message<?>> publisher() {
        return jetStreamClient.getOrEstablishConnection()
                .onItem().transformToMulti(this::publisher)
                .onFailure().invoke(throwable -> {
                    if (!isConsumerAlreadyInUse(throwable)) {
                        logger.errorf(throwable, "Failed to publish messages: %s", throwable.getMessage());
                        status.set(new Status(false, throwable.getMessage(), ConnectionEvent.CommunicationFailed));
                    }
                })
                .onFailure().retry().withBackOff(configuration.retryBackoff()).indefinitely()
                .onTermination().invoke(this::close)
                .onCancellation().invoke(this::close)
                .onCompletion().invoke(this::close);
    }

    @Override
    public Status getStatus() {
        return status.get();
    }

    @Override
    public void close() {
        jetStreamClient.close();
    }

    @Override
    public String getChannel() {
        return configuration.channel();
    }

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<?>> publisher(Connection connection) {
        boolean traceEnabled = configuration.traceEnabled();
        Class<?> payloadType = configuration.payloadType().orElse(null);
        return Multi.createFrom().<io.nats.client.Message> emitter(emitter -> {
            try {
                final var jetStream = connection.jetStream();
                final var subject = configuration.subject();
                dispatcher = connection.createDispatcher();
                final var pushOptions = optionsFactory.create(configuration);
                subscription = jetStream.subscribe(
                        subject, dispatcher,
                        emitter::emit,
                        false,
                        pushOptions);
            } catch (Throwable e) {
                logger.errorf(
                        e,
                        "Failed subscribing to stream: %s, subject: %s with message: %s",
                        configuration.stream(),
                        configuration.subject(),
                        e.getMessage());
                emitter.fail(e);
            }
        })
                .onTermination().invoke(() -> shutDown())
                .onCompletion().invoke(() -> shutDown())
                .onCancellation().invoke(() -> shutDown())
                .emitOn(runnable -> connection.context().runOnContext(runnable))
                .map(message -> messageFactory.create(
                        message,
                        traceEnabled,
                        payloadType, connection.context(),
                        new ExponentialBackoff(
                                configuration.exponentialBackoff(),
                                configuration.exponentialBackoffMaxDuration()),
                        configuration.ackTimeout()));
    }

    @Override
    public void onEvent(ConnectionEvent event, Connection connection, String message) {
        switch (event) {
            case Connected -> this.status.set(new Status(true, message, event));
            case Closed -> this.status.set(new Status(false, message, event));
            case Reconnected -> this.status.set(new Status(false, message, event)); // Lost connection to server, the subscription is dead
            case DiscoveredServers -> this.status.set(new Status(true, message, event));
            case Resubscribed -> this.status.set(new Status(true, message, event));
            case LameDuck -> this.status.set(new Status(false, message, event));
            case CommunicationFailed -> this.status.set(new Status(false, message, event));
        }
    }

    private void shutDown() {
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
        close();
    }
}
