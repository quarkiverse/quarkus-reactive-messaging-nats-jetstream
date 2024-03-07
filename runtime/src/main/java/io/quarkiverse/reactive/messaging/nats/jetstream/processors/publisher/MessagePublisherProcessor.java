package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import static io.smallrye.reactive.messaging.tracing.TracingUtils.traceIncoming;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamIncomingMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamTrace;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.Context;

public class MessagePublisherProcessor implements MessageProcessor {
    private final static Logger logger = Logger.getLogger(MessagePublisherProcessor.class);

    final static int CONSUMER_ALREADY_IN_USE = 10013;

    private final MessagePublisherConfiguration configuration;
    private final JetStreamClient jetStreamClient;
    private final PayloadMapper payloadMapper;
    private final Instrumenter<JetStreamTrace, Void> instrumenter;
    private final AtomicReference<Status> status;

    private volatile JetStreamSubscription subscription;
    private volatile Dispatcher dispatcher;
    private volatile boolean closed = false;

    public MessagePublisherProcessor(final JetStreamClient jetStreamClient,
            final MessagePublisherConfiguration configuration,
            final PayloadMapper payloadMapper,
            final JetStreamInstrumenter jetStreamInstrumenter) {
        this.configuration = configuration;
        this.jetStreamClient = jetStreamClient;
        this.payloadMapper = payloadMapper;
        this.instrumenter = jetStreamInstrumenter.receiver();
        this.status = new AtomicReference<>(new Status(false, "Not connected"));
    }

    @Override
    public Status getStatus() {
        return status.get();
    }

    @Override
    public void close() {
        try {
            if (subscription.isActive()) {
                subscription.drain(Duration.ofMillis(1000));
            }
        } catch (InterruptedException | IllegalStateException e) {
            logger.warnf("Interrupted while draining subscription");
        }
        try {
            if (subscription.isActive()) {
                subscription.unsubscribe();
            }
        } catch (IllegalStateException e) {
            logger.warnf("Failed to unsubscribe subscription");
        }
        closed = true;
        jetStreamClient.close();
    }

    @Override
    public String getChannel() {
        return configuration.getChannel();
    }

    public Multi<? extends Message<?>> getPublisher() {
        return jetStreamClient.getOrEstablishConnection()
                .onItem().transformToMulti(connection -> configuration.getPull() ? pull(connection) : push(connection))
                .onFailure().invoke(throwable -> {
                    if (!isConsumerAlreadyInUse(throwable)) {
                        logger.errorf(throwable, "Publish failure: %s", throwable.getMessage());
                    }
                    close();
                })
                .onFailure().retry().withBackOff(Duration.ofMillis(configuration.getRetryBackoff())).indefinitely()
                .onCompletion().invoke(this::close);
    }

    public Multi<org.eclipse.microprofile.reactive.messaging.Message<?>> push(Connection connection) {
        return Multi.createFrom().<io.nats.client.Message> emitter(emitter -> {
            try {
                final var jetStream = connection.jetStream();
                final var subject = configuration.getSubject();
                dispatcher = connection.createDispatcher();
                final var pushOptions = createPushSubscribeOptions(configuration);
                subscription = jetStream.subscribe(subject, dispatcher, emitter::emit, false, pushOptions);
                setStatus(true, "Is connected");
            } catch (JetStreamApiException e) {
                if (CONSUMER_ALREADY_IN_USE == e.getApiErrorCode()) {
                    setStatus(true, "Consumer already in use");
                    emitter.fail(e);
                } else {
                    logger.errorf(e, "Failed subscribing to stream with message: %s", e.getMessage());
                    setStatus(false, e.getMessage());
                    emitter.fail(e);
                }
            } catch (Throwable e) {
                logger.errorf(e, "Failed subscribing to stream with message: %s", e.getMessage());
                setStatus(false, e.getMessage());
                emitter.fail(e);
            }
        })
                .onTermination().invoke(() -> shutDown(dispatcher))
                .emitOn(runnable -> connection.context().runOnContext(runnable))
                .map(message -> create(message, connection.context(), configuration));
    }

    public Multi<? extends org.eclipse.microprofile.reactive.messaging.Message<?>> pull(Connection connection) {
        int batchSize = configuration.getPullBatchSize();
        int repullAt = configuration.getPullRepullAt();
        Duration pollTimeout = Duration.ofMillis(configuration.getPullPollTimeout());
        ExecutorService pullExecutor = Executors.newSingleThreadExecutor(JetstreamWorkerThread::new);
        try {
            var jetStream = connection.jetStream();
            var subject = configuration.getSubject();
            var pullSubscribeOptions = createPullSubscribeOptions(configuration);
            subscription = jetStream.subscribe(subject, pullSubscribeOptions);
            JetStreamReader reader = subscription.reader(batchSize, repullAt);
            setStatus(true, "Is connected");
            return Multi.createBy().repeating()
                    .supplier(() -> nextNatsMessage(reader, pollTimeout))
                    .until(message -> closed || !subscription.isActive())
                    .runSubscriptionOn(pullExecutor)
                    .onTermination().invoke(() -> shutDown(pullExecutor))
                    .emitOn(runnable -> connection.context().runOnContext(runnable))
                    .flatMap(message -> createMulti(message, connection.context(), configuration));
        } catch (Throwable e) {
            logger.errorf(e, "Failed subscribing to stream with message: %s", e.getMessage());
            setStatus(false, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private io.nats.client.Message nextNatsMessage(JetStreamReader reader, Duration pollTimeout) {
        io.nats.client.Message message = null;
        if (subscription.isActive()) {
            try {
                message = reader.nextMessage(pollTimeout);
            } catch (Throwable throwable) {
                logger.warnf("Error while pulling from the subscription %s: %s",
                        configuration.getChannel(), throwable.getMessage());
                if (logger.isTraceEnabled()) {
                    logger.tracef(throwable, "Error while pulling from the subscription %s",
                            configuration.getChannel());
                }
            }
        }
        return message;
    }

    private void setStatus(boolean healthy, String message) {
        this.status.set(new Status(healthy, message));
    }

    private org.eclipse.microprofile.reactive.messaging.Message<?> create(io.nats.client.Message message,
            Context context, MessagePublisherConfiguration configuration) {

        ExponentialBackoff exponentialBackoff = new ExponentialBackoff(configuration.getExponentialBackoff(),
                configuration.getExponentialBackoffMaxDuration());
        Class<?> payloadType = configuration.getType().map(PayloadMapper::loadClass).orElse(null);
        final var incomingMessage = payloadType != null
                ? new JetStreamIncomingMessage<>(message, payloadMapper.toPayload(message, payloadType), context,
                        exponentialBackoff)
                : new JetStreamIncomingMessage<>(message, payloadMapper.toPayload(message).orElse(null), context,
                        exponentialBackoff);

        if (configuration.traceEnabled()) {
            return traceIncoming(instrumenter, incomingMessage, JetStreamTrace.trace(incomingMessage));
        } else {
            return incomingMessage;
        }
    }

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<?>> createMulti(io.nats.client.Message message,
            Context context, MessagePublisherConfiguration configuration) {
        if (message == null || message.getData() == null) {
            return Multi.createFrom().empty();
        } else {
            return Multi.createFrom().item(() -> create(message, context, configuration));
        }
    }

    private boolean isConsumerAlreadyInUse(Throwable throwable) {
        if (throwable instanceof JetStreamApiException jetStreamApiException) {
            return jetStreamApiException.getApiErrorCode() == CONSUMER_ALREADY_IN_USE;
        }
        return false;
    }

    private PushSubscribeOptions createPushSubscribeOptions(final MessagePublisherConfiguration configuration) {
        final var deliverGroup = configuration.getDeliverGroup().orElse(null);
        final var durable = configuration.getDurable().orElse(null);
        final var backoff = getBackOff(configuration).orElse(null);
        final var maxDeliver = configuration.getMaxDeliver();
        return createPushSubscribeOptions(durable, deliverGroup, backoff, maxDeliver);
    }

    static PushSubscribeOptions createPushSubscribeOptions(final String durable, final String deliverGroup, String[] backoff,
            Long maxDeliever) {
        return PushSubscribeOptions.builder()
                .deliverGroup(deliverGroup)
                .durable(durable)
                .configuration(
                        ConsumerConfiguration.builder()
                                .maxDeliver(maxDeliever)
                                .backoff(getBackOff(backoff).orElse(null))
                                .build())
                .build();
    }

    private PullSubscribeOptions createPullSubscribeOptions(final MessagePublisherConfiguration configuration) {
        final var durable = configuration.getDurable().orElse(null);
        final var backoff = getBackOff(configuration).orElse(null);
        final var maxDeliver = configuration.getMaxDeliver();
        return PullSubscribeOptions.builder()
                .durable(durable)
                .configuration(
                        ConsumerConfiguration.builder()
                                .maxDeliver(maxDeliver)
                                .backoff(getBackOff(backoff).orElse(null))
                                .build())
                .build();
    }

    private Optional<String[]> getBackOff(final MessagePublisherConfiguration configuration) {
        return configuration.getBackOff().map(backoff -> backoff.split(","));
    }

    private static Optional<Duration[]> getBackOff(String[] backoff) {
        if (backoff == null || backoff.length == 0) {
            return Optional.empty();
        } else {
            return Optional.of(Arrays.stream(backoff).map(MessagePublisherProcessor::toDuration).toList()
                    .toArray(new Duration[] {}));
        }
    }

    private static Duration toDuration(String value) {
        return Duration.parse(value);
    }

    private void shutDown(ExecutorService pullExecutor) {
        try {
            pullExecutor.shutdownNow();
        } catch (Exception e) {
            logger.errorf(e, "Failed to shutdown pull executor");
        }
        close();
    }

    private void shutDown(Dispatcher dispatcher) {
        try {
            if (dispatcher != null && dispatcher.isActive()) {
                dispatcher.unsubscribe(subscription);
            }
        } catch (Exception e) {
            logger.errorf(e, "Failed to shutdown pull executor");
        }
        close();
    }
}
