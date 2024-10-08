package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.JetStreamStatusException;
import io.nats.client.JetStreamSubscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamState;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.FetchConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PublishConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullSubscribeOptionsFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ReaderConsumerConfiguration;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;

public class ReaderSubscribeConnection<P> implements SubscribeConnection<P> {
    private final static Logger logger = Logger.getLogger(ReaderSubscribeConnection.class);

    private final DefaultConnection delegate;
    private final ReaderConsumerConfiguration<P> consumerConfiguration;
    private final io.nats.client.JetStreamReader reader;
    private final JetStreamSubscription subscription;

    ReaderSubscribeConnection(DefaultConnection delegate,
            ReaderConsumerConfiguration<P> consumerConfiguration) throws ConnectionException {
        this.delegate = delegate;
        this.consumerConfiguration = consumerConfiguration;
        try {
            final var jetStream = delegate.connection().jetStream();
            final var optionsFactory = new PullSubscribeOptionsFactory();
            this.subscription = jetStream.subscribe(consumerConfiguration.subject(),
                    optionsFactory.create(consumerConfiguration));
            this.reader = subscription.reader(consumerConfiguration.maxRequestBatch(), consumerConfiguration.rePullAt());
        } catch (Throwable failure) {
            throw new ConnectionException(failure);
        }
    }

    @Override
    public Multi<Message<P>> subscribe() {
        boolean traceEnabled = consumerConfiguration.consumerConfiguration().traceEnabled();
        Class<P> payloadType = consumerConfiguration.consumerConfiguration().payloadType().orElse(null);
        ExecutorService pullExecutor = Executors.newSingleThreadExecutor(JetstreamWorkerThread::new);
        return Multi.createBy().repeating()
                .uni(this::readNextMessage)
                .whilst(message -> isConnected() && subscription.isActive())
                .runSubscriptionOn(pullExecutor)
                .emitOn(runable -> delegate.context().runOnContext(runable))
                .flatMap(message -> createMulti(message.orElse(null), traceEnabled, payloadType, delegate.context()))
                .onCompletion().invoke(() -> fireEvent(ConnectionEvent.SubscriptionInactive, "Subscription became inactive"));
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

    @Override
    public List<ConnectionListener> listeners() {
        return delegate.listeners();
    }

    @Override
    public void addListener(ConnectionListener listener) {
        delegate.addListener(listener);
    }

    @Override
    public Uni<Consumer> getConsumer(String stream, String consumerName) {
        return delegate.getConsumer(stream, consumerName);
    }

    @Override
    public Uni<List<String>> getStreams() {
        return delegate.getStreams();
    }

    @Override
    public Uni<List<String>> getSubjects(String streamName) {
        return delegate.getSubjects(streamName);
    }

    @Override
    public Uni<List<String>> getConsumerNames(String streamName) {
        return delegate.getConsumerNames(streamName);
    }

    @Override
    public Uni<PurgeResult> purgeStream(String streamName) {
        return delegate.purgeStream(streamName);
    }

    @Override
    public Uni<Void> deleteMessage(String stream, long sequence, boolean erase) {
        return delegate.deleteMessage(stream, sequence, erase);
    }

    @Override
    public Uni<StreamState> getStreamState(String streamName) {
        return delegate.getStreamState(streamName);
    }

    @Override
    public Uni<List<PurgeResult>> purgeAllStreams() {
        return delegate.purgeAllStreams();
    }

    @Override
    public <T> Uni<Message<T>> publish(Message<T> message, PublishConfiguration configuration) {
        return delegate.publish(message, configuration);
    }

    @Override
    public <T> Uni<Message<T>> publish(Message<T> message, PublishConfiguration publishConfiguration,
            FetchConsumerConfiguration<T> consumerConfiguration) {
        return delegate.publish(message, publishConfiguration, consumerConfiguration);
    }

    @Override
    public <T> Uni<Message<T>> nextMessage(FetchConsumerConfiguration<T> configuration) {
        return delegate.nextMessage(configuration);
    }

    @Override
    public <T> Multi<Message<T>> nextMessages(FetchConsumerConfiguration<T> configuration) {
        return delegate.nextMessages(configuration);
    }

    @Override
    public <T> Uni<T> getKeyValue(String bucketName, String key, Class<T> valueType) {
        return delegate.getKeyValue(bucketName, key, valueType);
    }

    @Override
    public <T> Uni<Void> putKeyValue(String bucketName, String key, T value) {
        return delegate.putKeyValue(bucketName, key, value);
    }

    @Override
    public Uni<Void> deleteKeyValue(String bucketName, String key) {
        return delegate.deleteKeyValue(bucketName, key);
    }

    @Override
    public <T> Uni<Message<T>> resolve(String streamName, long sequence) {
        return delegate.resolve(streamName, sequence);
    }

    @Override
    public Uni<Void> flush(Duration duration) {
        return delegate.flush(duration);
    }

    @Override
    public void close() {
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
        delegate.close();
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
            } catch (Throwable throwable) {
                emitter.fail(new ReaderException(String.format("Error reading next message from stream: %s",
                        consumerConfiguration.consumerConfiguration().stream()), throwable));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Multi<org.eclipse.microprofile.reactive.messaging.Message<P>> createMulti(io.nats.client.Message message,
            boolean tracingEnabled, Class<P> payloadType, Context context) {
        if (message == null || message.getData() == null) {
            return Multi.createFrom().empty();
        } else {
            return Multi.createFrom()
                    .item(() -> delegate.messageMapper().of(message, tracingEnabled, payloadType, context,
                            new ExponentialBackoff(
                                    consumerConfiguration.consumerConfiguration().exponentialBackoff(),
                                    consumerConfiguration.consumerConfiguration().exponentialBackoffMaxDuration()),
                            consumerConfiguration.consumerConfiguration().ackTimeout()));
        }
    }
}
