package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import static io.smallrye.reactive.messaging.tracing.TracingUtils.traceIncoming;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;

import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamIncomingMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamReader;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamTrace;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.Context;

public class MessagePullPublisherProcessor implements MessagePublisherProcessor {

    private final static Logger logger = Logger.getLogger(MessagePullPublisherProcessor.class);

    final static int CONSUMER_ALREADY_IN_USE = 10013;

    private final MessagePullPublisherConfiguration configuration;
    private final JetStreamClient jetStreamClient;
    private final PayloadMapper payloadMapper;
    private final Instrumenter<JetStreamTrace, Void> instrumenter;
    private final AtomicReference<Status> status;

    private volatile JetStreamReader jetStreamReader;

    public MessagePullPublisherProcessor(final JetStreamClient jetStreamClient,
            final MessagePullPublisherConfiguration configuration,
            final PayloadMapper payloadMapper,
            final JetStreamInstrumenter jetStreamInstrumenter) {
        this.configuration = configuration;
        this.jetStreamClient = jetStreamClient;
        this.payloadMapper = payloadMapper;
        this.instrumenter = jetStreamInstrumenter.receiver();
        this.status = new AtomicReference<>(new Status(false, "Not connected"));
    }

    @Override
    public JetStreamClient jetStreamClient() {
        return jetStreamClient;
    }

    @Override
    public MessagePublisherConfiguration configuration() {
        return configuration;
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
    public Multi<org.eclipse.microprofile.reactive.messaging.Message<?>> publish(Connection connection) {
        boolean traceEnabled = configuration.traceEnabled();
        Class<?> payloadType = configuration.payloadType().orElse(null);
        ExecutorService pullExecutor = Executors.newSingleThreadExecutor(JetstreamWorkerThread::new);
        try {
            jetStreamReader = new JetStreamReader(connection, configuration);
            setStatus(true, "Is connected");
            return Multi.createBy().repeating()
                    .supplier(() -> jetStreamReader.nextMessage())
                    .until(message -> !jetStreamReader.isActive())
                    .runSubscriptionOn(pullExecutor)
                    .onTermination().invoke(() -> shutDown(pullExecutor))
                    .onCompletion().invoke(() -> shutDown(pullExecutor))
                    .onCancellation().invoke(() -> shutDown(pullExecutor))
                    .emitOn(runnable -> connection.context().runOnContext(runnable))
                    .flatMap(message -> createMulti(message, traceEnabled, payloadType, connection.context()));
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
            boolean tracingEnabled,
            Class<?> payloadType,
            Context context,
            MessagePublisherConfiguration configuration) {
        final var exponentialBackoff = new ExponentialBackoff(configuration.exponentialBackoff(),
                configuration.exponentialBackoffMaxDuration());
        final var incomingMessage = payloadType != null
                ? new JetStreamIncomingMessage<>(message, payloadMapper.toPayload(message, payloadType), context,
                        exponentialBackoff)
                : new JetStreamIncomingMessage<>(message, payloadMapper.toPayload(message).orElse(null), context,
                        exponentialBackoff);
        if (tracingEnabled) {
            return traceIncoming(instrumenter, incomingMessage, JetStreamTrace.trace(incomingMessage));
        } else {
            return incomingMessage;
        }
    }

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<?>> createMulti(io.nats.client.Message message,
            boolean tracingEnabled, Class<?> payloadType, Context context) {
        if (message == null || message.getData() == null) {
            return Multi.createFrom().empty();
        } else {
            return Multi.createFrom().item(() -> create(message, tracingEnabled, payloadType, context, configuration));
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
