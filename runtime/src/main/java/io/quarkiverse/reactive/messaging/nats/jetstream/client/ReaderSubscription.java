package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.JetStreamReader;
import io.nats.client.JetStreamStatusException;
import io.nats.client.JetStreamSubscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessage;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ReaderConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.Tracer;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;

class ReaderSubscription<P> implements Subscription<P> {
    private final static Logger logger = Logger.getLogger(ReaderSubscription.class);

    private final Connection connection;
    private final ReaderConsumerConfiguration<P> consumerConfiguration;
    private final JetStreamReader reader;
    private final JetStreamSubscription subscription;
    private final MessageMapper messageMapper;
    private final AtomicBoolean closed;

    ReaderSubscription(Connection connection,
            ReaderConsumerConfiguration<P> consumerConfiguration,
            JetStreamSubscription subscription,
            JetStreamReader reader,
            MessageMapper messageMapper) {
        this.connection = connection;
        this.consumerConfiguration = consumerConfiguration;
        this.subscription = subscription;
        this.reader = reader;
        this.messageMapper = messageMapper;
        this.closed = new AtomicBoolean(false);
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public Multi<Message<P>> subscribe(Tracer<P> tracer, Context context) {
        Class<P> payloadType = consumerConfiguration.consumerConfiguration().payloadType().orElse(null);
        ExecutorService pullExecutor = Executors.newSingleThreadExecutor(JetstreamWorkerThread::new);
        return Multi.createBy().repeating()
                .uni(this::readNextMessage)
                .whilst(message -> true)
                .runSubscriptionOn(pullExecutor)
                .flatMap(message -> createMulti(message.orElse(null), payloadType, context))
                .onItem().transformToUniAndMerge(tracer::withTrace);
    }

    @Override
    public void onEvent(ConnectionEvent event, String message) {

    }

    @Override
    public void close() {
        this.closed.set(true);
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
        connection.removeListener(this);
    }

    private Uni<Optional<io.nats.client.Message>> readNextMessage() {
        return Uni.createFrom().emitter(emitter -> {
            try {
                emitter.complete(Optional
                        .ofNullable(reader.nextMessage(consumerConfiguration.maxRequestExpires().orElse(Duration.ZERO))));
            } catch (JetStreamStatusException e) {
                emitter.fail(new ReaderException(e));
            } catch (IllegalStateException e) {
                logger.warnf("The subscription became inactive for stream: %s",
                        consumerConfiguration.consumerConfiguration().stream());
                emitter.complete(Optional.empty());
            } catch (InterruptedException e) {
                emitter.fail(new ReaderException(String.format("The reader was interrupted for stream: %s",
                        consumerConfiguration.consumerConfiguration().stream()), e));
            } catch (Exception exception) {
                emitter.fail(new ReaderException(String.format("Error reading next message from stream: %s",
                        consumerConfiguration.consumerConfiguration().stream()), exception));
            }
        });
    }

    private Multi<PublishMessage<P>> createMulti(io.nats.client.Message message,
            Class<P> payloadType, Context context) {
        if (message == null || message.getData() == null) {
            return Multi.createFrom().empty();
        } else {
            return Multi.createFrom()
                    .item(() -> messageMapper.of(message, payloadType, context,
                            new ExponentialBackoff(
                                    consumerConfiguration.consumerConfiguration().exponentialBackoff(),
                                    consumerConfiguration.consumerConfiguration().exponentialBackoffMaxDuration()),
                            consumerConfiguration.consumerConfiguration().ackTimeout()));
        }
    }
}
