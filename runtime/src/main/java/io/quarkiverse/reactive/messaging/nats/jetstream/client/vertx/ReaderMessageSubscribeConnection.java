package io.quarkiverse.reactive.messaging.nats.jetstream.client.vertx;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.JetStreamStatusException;
import io.nats.client.JetStreamSubscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetstreamWorkerThread;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.MessageSubscribeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullSubscribeOptionsFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ReaderConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;

public class ReaderMessageSubscribeConnection<K> extends MessageConnection implements MessageSubscribeConnection {
    private final static Logger logger = Logger.getLogger(ReaderMessageSubscribeConnection.class);

    private final ReaderConsumerConfiguration<K> consumerConfiguration;
    private final io.nats.client.JetStreamReader reader;
    private final JetStreamSubscription subscription;

    public ReaderMessageSubscribeConnection(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener,
            Context context,
            JetStreamInstrumenter instrumenter,
            ReaderConsumerConfiguration<K> consumerConfiguration,
            MessageFactory messageFactory,
            PayloadMapper payloadMapper) throws ConnectionException {
        super(connectionConfiguration, connectionListener, messageFactory, context, instrumenter, payloadMapper);
        this.consumerConfiguration = consumerConfiguration;
        try {
            final var jetStream = connection.jetStream();
            final var optionsFactory = new PullSubscribeOptionsFactory();
            this.subscription = jetStream.subscribe(consumerConfiguration.subject(),
                    optionsFactory.create(consumerConfiguration));
            this.reader = subscription.reader(consumerConfiguration.maxRequestBatch(), consumerConfiguration.rePullAt());
        } catch (Throwable failure) {
            throw new ReaderException(failure);
        }
    }

    @Override
    public Multi<Message<?>> subscribe() {
        boolean traceEnabled = consumerConfiguration.consumerConfiguration().traceEnabled();
        Class<?> payloadType = consumerConfiguration.consumerConfiguration().payloadType().orElse(null);
        ExecutorService pullExecutor = Executors.newSingleThreadExecutor(JetstreamWorkerThread::new);
        return Multi.createBy().repeating()
                .supplier(this::nextMessage)
                .until(message -> !subscription.isActive())
                .runSubscriptionOn(pullExecutor)
                .emitOn(context::runOnContext)
                .flatMap(message -> createMulti(message.orElse(null), traceEnabled, payloadType, context));
    }

    @Override
    public Uni<Void> flush(Duration duration) {
        return super.flush(duration)
                .emitOn(context::runOnContext);
    }

    @Override
    public void close() throws Exception {
        try {
            reader.stop();
        } catch (Throwable e) {
            logger.warnf("Failed to stop reader with message %s", e.getMessage());
        }
        try {
            if (subscription.isActive()) {
                subscription.drain(Duration.ofMillis(1000));
            }
        } catch (Throwable e) {
            logger.warnf("Interrupted while draining subscription");
        }
        try {
            if (subscription.isActive()) {
                subscription.unsubscribe();
            }
        } catch (Throwable e) {
            logger.warnf("Failed to unsubscribe subscription with message %s", e.getMessage());
        }
        super.close();
    }

    private Optional<io.nats.client.Message> nextMessage() {
        try {
            return Optional.ofNullable(reader.nextMessage(consumerConfiguration.maxRequestExpires().orElse(Duration.ZERO)));
        } catch (JetStreamStatusException e) {
            logger.debugf(e, e.getMessage());
            return Optional.empty();
        } catch (IllegalStateException e) {
            logger.debugf(e, "The subscription became inactive for stream: %s",
                    consumerConfiguration.consumerConfiguration().stream());
            return Optional.empty();
        } catch (InterruptedException e) {
            logger.debugf(e, "The reader was interrupted for stream: %s",
                    consumerConfiguration.consumerConfiguration().stream());
            return Optional.empty();
        } catch (Throwable throwable) {
            logger.warnf(throwable, "Error reading next message from stream: %s",
                    consumerConfiguration.consumerConfiguration().stream());
            return Optional.empty();
        }
    }

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<K>> createMulti(io.nats.client.Message message,
            boolean tracingEnabled, Class<?> payloadType, Context context) {
        if (message == null || message.getData() == null) {
            return Multi.createFrom().empty();
        } else {
            return Multi.createFrom()
                    .item(() -> messageFactory.create(message, tracingEnabled, payloadType, context, new ExponentialBackoff(
                            consumerConfiguration.consumerConfiguration().exponentialBackoff(),
                            consumerConfiguration.consumerConfiguration().exponentialBackoffMaxDuration()),
                            consumerConfiguration.consumerConfiguration().ackTimeout()));
        }
    }
}
