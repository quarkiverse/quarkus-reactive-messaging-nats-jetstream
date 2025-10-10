package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamState;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.MessageMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher.PublishListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher.PublisherAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher.PublisherAwareImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreAwareImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Duration;
import java.time.ZonedDateTime;

@JBossLog
@ApplicationScoped
public class ClientImpl implements Client {
    private final PublisherAware publisherDelegate;
    private final ConsumerAware consumerDelegate;
    private final StreamAware streamDelegate;
    private final KeyValueStoreAware keyValueStoreDelegate;

    public ClientImpl(ExecutionHolder executionHolder,
                      Connection connection,
                      TracerFactory tracerFactory,
                      PayloadMapper payloadMapper,
                      ConsumerConfigurationMapper consumerConfigurationMapper,
                      ConsumerMapper consumerMapper,
                      MessageMapper messageMapper,
                      StreamStateMapper streamStateMapper,
                      StreamConfigurationMapper streamConfigurationMapper,
                      KeyValueConfigurationMapper keyValueConfigurationMapper) {
        this.publisherDelegate = new PublisherAwareImpl(executionHolder, tracerFactory, payloadMapper, connection);
        this.consumerDelegate = new ConsumerAwareImpl(executionHolder, consumerConfigurationMapper, consumerMapper, tracerFactory, messageMapper, payloadMapper, connection);
        this.streamDelegate = new StreamAwareImpl(executionHolder, streamStateMapper, streamConfigurationMapper, connection);
        this.keyValueStoreDelegate = new KeyValueStoreAwareImpl(executionHolder, payloadMapper, keyValueConfigurationMapper, connection);
    }

    @Override
    public <T> Uni<Message<T>> publish(Message<T> message, String stream, String subject) {
        return publisherDelegate.publish(message, stream, subject);
    }

    @Override
    public <T> Uni<Message<T>> publish(Message<T> message, String stream, String subject, PublishListener listener) {
        return publisherDelegate.publish(message, stream, subject, listener);
    }

    @Override
    public <T> Multi<Message<T>> publish(Multi<Message<T>> messages, String stream, String subject) {
        return publisherDelegate.publish(messages, stream, subject);
    }

    @Override
    public <T> Multi<Message<T>> publish(Multi<Message<T>> messages, String stream, String subject, PublishListener listener) {
        return publisherDelegate.publish(messages, stream, subject, listener);
    }

    @Override
    public <T> Uni<Consumer> addConsumerIfAbsent(ConsumerConfiguration<T> configuration) {
        return consumerDelegate.addConsumerIfAbsent(configuration);
    }

    @Override
    public <T> Uni<Consumer> addConsumerIfAbsent(ConsumerConfiguration<T> configuration, PushConfiguration pushConfiguration) {
        return consumerDelegate.addConsumerIfAbsent(configuration, pushConfiguration);
    }

    @Override
    public <T> Uni<Consumer> addConsumerIfAbsent(ConsumerConfiguration<T> configuration, PullConfiguration pullConfiguration) {
        return consumerDelegate.addConsumerIfAbsent(configuration, pullConfiguration);
    }

    @Override
    public Uni<Consumer> consumer(String stream, String consumerName) {
        return consumerDelegate.consumer(stream, consumerName);
    }

    @Override
    public Multi<String> consumerNames(String streamName) {
        return consumerDelegate.consumerNames(streamName);
    }

    @Override
    public Uni<Void> deleteConsumer(String streamName, String consumerName) {
        return consumerDelegate.deleteConsumer(streamName, consumerName);
    }

    @Override
    public Uni<Void> pauseConsumer(String streamName, String consumerName, ZonedDateTime pauseUntil) {
        return consumerDelegate.pauseConsumer(streamName, consumerName, pauseUntil);
    }

    @Override
    public Uni<Void> resumeConsumer(String streamName, String consumerName) {
        return consumerDelegate.resumeConsumer(streamName, consumerName);
    }

    @Override
    public <T> Uni<Message<T>> next(ConsumerConfiguration<T> configuration, Duration timeout) {
        return consumerDelegate.next(configuration, timeout);
    }

    @Override
    public <T> Multi<Message<T>> fetch(ConsumerConfiguration<T> configuration, FetchConfiguration fetchConfiguration) {
        return consumerDelegate.fetch(configuration, fetchConfiguration);
    }

    @Override
    public <T> Uni<Message<T>> resolve(String stream, long sequence) {
        return consumerDelegate.resolve(stream, sequence);
    }

    @Override
    public <T> Multi<Message<T>> subscribe(ConsumerConfiguration<T> configuration, PullConfiguration pullConfiguration) {
        return consumerDelegate.subscribe(configuration, pullConfiguration);
    }

    @Override
    public <T> Multi<Message<T>> subscribe(ConsumerConfiguration<T> configuration, PushConfiguration pushConfiguration) {
        return consumerDelegate.subscribe(configuration, pushConfiguration);
    }

    @Override
    public <T> Multi<Message<T>> subscribe(ConsumerConfiguration<T> configuration, PullConfiguration pullConfiguration,
                                           ConsumerListener<T> listener) {
        return consumerDelegate.subscribe(configuration, pullConfiguration, listener);
    }

    @Override
    public <T> Multi<Message<T>> subscribe(ConsumerConfiguration<T> configuration, PushConfiguration pushConfiguration,
                                           ConsumerListener<T> listener) {
        return consumerDelegate.subscribe(configuration, pushConfiguration, listener);
    }

    @Override
    public Multi<String> streamNames() {
        return streamDelegate.streamNames();
    }

    @Override
    public Multi<String> subjects(String streamName) {
        return streamDelegate.subjects(streamName);
    }

    @Override
    public Uni<PurgeResult> purge(String streamName) {
        return streamDelegate.purge(streamName);
    }

    @Override
    public Uni<Long> firstSequence(String streamName) {
        return streamDelegate.firstSequence(streamName);
    }

    @Override
    public Uni<Void> deleteMessage(String stream, long sequence, boolean erase) {
        return streamDelegate.deleteMessage(stream, sequence, erase);
    }

    @Override
    public Uni<StreamState> streamState(String streamName) {
        return streamDelegate.streamState(streamName);
    }

    @Override
    public Uni<StreamConfiguration> streamConfiguration(String streamName) {
        return streamDelegate.streamConfiguration(streamName);
    }

    @Override
    public Multi<PurgeResult> purgeAll() {
        return streamDelegate.purgeAll();
    }

    @Override
    public Uni<Void> addSubject(String streamName, String subject) {
        return streamDelegate.addSubject(streamName, subject);
    }

    @Override
    public Uni<Void> removeSubject(String streamName, String subject) {
        return streamDelegate.removeSubject(streamName, subject);
    }

    @Override
    public Uni<StreamResult> addStreamIfAbsent(StreamConfiguration configuration) {
        return streamDelegate.addStreamIfAbsent(configuration);
    }

    @Override
    public Uni<Void> addKeyValueStoreIfAbsent(KeyValueStoreConfiguration configuration) {
        return keyValueStoreDelegate.addKeyValueStoreIfAbsent(configuration);
    }

    @Override
    public <T> Uni<T> getValue(String bucketName, String key, Class<T> valueType) {
        return keyValueStoreDelegate.getValue(bucketName, key, valueType);
    }

    @Override
    public <T> Uni<Long> putValue(String bucketName, String key, T value) {
        return keyValueStoreDelegate.putValue(bucketName, key, value);
    }

    @Override
    public Uni<Void> deleteValue(String bucketName, String key) {
        return keyValueStoreDelegate.deleteValue(bucketName, key);
    }
}
