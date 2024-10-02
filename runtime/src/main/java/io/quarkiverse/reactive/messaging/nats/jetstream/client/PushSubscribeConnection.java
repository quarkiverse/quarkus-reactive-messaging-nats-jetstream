package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.Dispatcher;
import io.nats.client.JetStreamSubscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamState;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class PushSubscribeConnection<K> implements SubscribeConnection {
    private static final Logger logger = Logger.getLogger(PushSubscribeConnection.class);

    private final DefaultConnection delegate;
    private final PushConsumerConfiguration<K> consumerConfiguration;
    private final PushSubscribeOptionsFactory pushSubscribeOptionsFactory;

    private volatile JetStreamSubscription subscription;
    private volatile Dispatcher dispatcher;

    PushSubscribeConnection(DefaultConnection delegate,
            PushConsumerConfiguration<K> consumerConfiguration) {
        this.delegate = delegate;
        this.consumerConfiguration = consumerConfiguration;
        this.pushSubscribeOptionsFactory = new PushSubscribeOptionsFactory();
    }

    @Override
    public Multi<Message<?>> subscribe() {
        boolean traceEnabled = consumerConfiguration.consumerConfiguration().traceEnabled();
        Class<?> payloadType = consumerConfiguration.consumerConfiguration().payloadType().orElse(null);
        final var subject = consumerConfiguration.subject();
        return Multi.createFrom().<io.nats.client.Message> emitter(emitter -> {
            try {
                final var jetStream = delegate.connection().jetStream();
                dispatcher = delegate.connection().createDispatcher();
                final var pushOptions = pushSubscribeOptionsFactory.create(consumerConfiguration);
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
                .emitOn(runnable -> delegate.context().runOnContext(runnable))
                .map(message -> delegate.messageMapper().of(
                        message,
                        traceEnabled,
                        payloadType,
                        delegate.context(),
                        new ExponentialBackoff(
                                consumerConfiguration.consumerConfiguration().exponentialBackoff(),
                                consumerConfiguration.consumerConfiguration().exponentialBackoffMaxDuration()),
                        consumerConfiguration.consumerConfiguration().ackTimeout()));
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

    @Override
    public Uni<Void> flush(Duration duration) {
        return delegate.flush(duration);
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
        delegate.close();
    }
}
