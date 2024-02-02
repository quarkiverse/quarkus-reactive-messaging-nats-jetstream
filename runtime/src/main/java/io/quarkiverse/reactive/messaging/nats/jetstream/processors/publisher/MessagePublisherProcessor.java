package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import static io.smallrye.reactive.messaging.tracing.TracingUtils.traceIncoming;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamReader;
import io.nats.client.JetStreamSubscription;
import io.nats.client.PullSubscribeOptions;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamIncomingMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamTrace;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
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
            subscription.drain(Duration.ofMillis(1000));
        } catch (InterruptedException e) {
            logger.errorf("Interrupted while draining subscription");
        }
        subscription.unsubscribe();
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
        boolean traceEnabled = configuration.traceEnabled();
        Class<?> payloadType = configuration.getType().map(PayloadMapper::loadClass).orElse(null);
        return Multi.createFrom().<io.nats.client.Message> emitter(emitter -> {
            try {
                final var jetStream = connection.jetStream();
                final var subject = configuration.getSubject();
                final var dispatcher = connection.createDispatcher();
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
        }).emitOn(runnable -> connection.context().runOnContext(runnable))
                .map(message -> create(message, traceEnabled, payloadType, connection.context()));
    }

    public Multi<org.eclipse.microprofile.reactive.messaging.Message<?>> pull(Connection connection) {
        boolean traceEnabled = configuration.traceEnabled();
        int batchSize = configuration.getPullBatchSize();
        int repullAt = configuration.getPullRepullAt();
        Duration pollTimeout = Duration.ofMillis(configuration.getPullPollTimeout());
        Class<?> payloadType = configuration.getType().map(PayloadMapper::loadClass).orElse(null);
        ExecutorService pullExecutor = Executors.newSingleThreadExecutor(JetstreamWorkerThread::new);
        try {
            var jetStream = connection.jetStream();
            var subject = configuration.getSubject();
            var pullSubscribeOptions = createPullSubscribeOptions(configuration);
            subscription = jetStream.subscribe(subject, pullSubscribeOptions);
            JetStreamReader reader = subscription.reader(batchSize, repullAt);
            setStatus(true, "Is connected");
            return Multi.createBy().repeating().uni(() -> Uni.createFrom().<io.nats.client.Message> emitter(e -> {
                if (subscription.isActive()) {
                    try {
                        e.complete(reader.nextMessage(pollTimeout));
                    } catch (Throwable throwable) {
                        // log and continue
                        logger.warnf("Error while pulling from the subscription %s: %s",
                                configuration.getChannel(), throwable.getMessage());
                        if (logger.isTraceEnabled()) {
                            logger.tracef(throwable, "Error while pulling from the subscription %s",
                                    configuration.getChannel());
                        }
                    }
                }
            }).runSubscriptionOn(pullExecutor))
                    .until(message -> closed || !subscription.isActive())
                    .onTermination().invoke(pullExecutor::shutdownNow)
                    .emitOn(runnable -> connection.context().runOnContext(runnable))
                    .map(message -> create(message, traceEnabled, payloadType, connection.context()));
        } catch (Throwable e) {
            logger.errorf(e, "Failed subscribing to stream with message: %s", e.getMessage());
            setStatus(false, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void setStatus(boolean healthy, String message) {
        this.status.set(new Status(healthy, message));
    }

    private org.eclipse.microprofile.reactive.messaging.Message<?> create(io.nats.client.Message message,
            boolean tracingEnabled, Class<?> payloadType, Context context) {
        final var incomingMessage = payloadType != null
                ? new JetStreamIncomingMessage<>(message, payloadMapper.toPayload(message, payloadType), context)
                : new JetStreamIncomingMessage<>(message, payloadMapper.toPayload(message).orElse(null), context);
        if (tracingEnabled) {
            return traceIncoming(instrumenter, incomingMessage, JetStreamTrace.trace(incomingMessage));
        } else {
            return incomingMessage;
        }
    }

    private boolean isConsumerAlreadyInUse(Throwable throwable) {
        if (throwable instanceof JetStreamApiException) {
            final var jetStreamApiException = (JetStreamApiException) throwable;
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
            return Optional.of(Arrays.stream(backoff).map(MessagePublisherProcessor::toDuration).collect(Collectors.toList())
                    .toArray(new Duration[] {}));
        }
    }

    private static Duration toDuration(String value) {
        return Duration.parse(value);
    }

}
