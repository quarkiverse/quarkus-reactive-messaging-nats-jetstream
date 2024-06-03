package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionEvent;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.io.JetStreamReader;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.io.MessageFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.Context;

public class MessagePullPublisherProcessor implements MessagePublisherProcessor {
    private final static Logger logger = Logger.getLogger(MessagePullPublisherProcessor.class);

    private final MessagePullPublisherConfiguration<?> configuration;
    private final JetStreamClient jetStreamClient;
    private final AtomicReference<Status> status;
    private final MessageFactory messageFactory;

    private volatile JetStreamReader jetStreamReader;

    public MessagePullPublisherProcessor(final JetStreamClient jetStreamClient,
            final MessagePullPublisherConfiguration<?> configuration,
            final MessageFactory messageFactory) {
        this.configuration = configuration;
        this.jetStreamClient = jetStreamClient;
        this.messageFactory = messageFactory;
        this.status = new AtomicReference<>(new Status(false, "Not connected", ConnectionEvent.Closed));
    }

    @Override
    public Status getStatus() {
        return status.get();
    }

    @Override
    public void close() {
        if (jetStreamReader != null) {
            jetStreamReader.close();
        }
        jetStreamClient.close();
    }

    @Override
    public String getChannel() {
        return configuration.channel();
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
    public void onEvent(ConnectionEvent event, Connection connection, String message) {
        switch (event) {
            case Connected -> this.status.set(new Status(true, message, event));
            case Closed -> this.status.set(new Status(false, message, event));
            case Reconnected -> this.status.set(new Status(false, message, event));
            case DiscoveredServers -> this.status.set(new Status(true, message, event));
            case Resubscribed -> this.status.set(new Status(true, message, event));
            case LameDuck -> this.status.set(new Status(false, message, event));
            case CommunicationFailed -> this.status.set(new Status(false, message, event));
        }
    }

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<?>> publisher(Connection connection) {
        boolean traceEnabled = configuration.traceEnabled();
        Class<?> payloadType = configuration.payloadType().orElse(null);
        ExecutorService pullExecutor = Executors.newSingleThreadExecutor(JetstreamWorkerThread::new);
        jetStreamReader = new JetStreamReader(connection, configuration);
        jetStreamClient.addListener(jetStreamReader);
        return Multi.createBy().repeating()
                .supplier(() -> jetStreamReader.nextMessage())
                .until(message -> {
                    if (jetStreamReader.isActive()) {
                        return false;
                    } else {
                        this.status.set(new Status(false, "Reader become inactive", ConnectionEvent.CommunicationFailed));
                        return true;
                    }
                })
                .runSubscriptionOn(pullExecutor)
                .onTermination().invoke(() -> shutDown(pullExecutor))
                .onCompletion().invoke(() -> shutDown(pullExecutor))
                .onCancellation().invoke(() -> shutDown(pullExecutor))
                .emitOn(runnable -> connection.context().runOnContext(runnable))
                .flatMap(message -> createMulti(message, traceEnabled, payloadType, connection.context()));

    }

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<?>> createMulti(io.nats.client.Message message,
            boolean tracingEnabled, Class<?> payloadType, Context context) {
        if (message == null || message.getData() == null) {
            return Multi.createFrom().empty();
        } else {
            return Multi.createFrom()
                    .item(() -> messageFactory.create(message, tracingEnabled, payloadType, context, new ExponentialBackoff(
                            configuration.exponentialBackoff(), configuration.exponentialBackoffMaxDuration()),
                            configuration.ackTimeout()));
        }
    }

    private void shutDown(ExecutorService pullExecutor) {
        try {
            pullExecutor.shutdownNow();
        } catch (Exception e) {
            logger.errorf(e, "Failed to shutdown pull executor");
        }
        close();
    }
}
