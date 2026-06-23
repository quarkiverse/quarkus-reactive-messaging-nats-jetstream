package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamInfoMapper;
import io.smallrye.mutiny.unchecked.Unchecked;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.Operation;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;
import lombok.extern.jbosslog.JBossLog;
import org.mapstruct.factory.Mappers;

@JBossLog
class VertxClient implements Client {
    private final Connection connection;
    private final Publisher publisher;
    private final Consumer consumer;
    private final StreamManagement management;
    private final StreamInfoMapper streamInfoMapper;
    private final Context context;
    private final ClientConfiguration configuration;

    public VertxClient(ClientConfiguration configuration, Connection connection, Context context, TracerFactory tracerFactory) {
        this.connection = connection;
        this.streamInfoMapper = Mappers.getMapper(StreamInfoMapper.class);
        this.context = context;
        this.configuration = configuration;

        final var consumerConfigurationMapper = Mappers.getMapper(ConsumerConfigurationMapper.class);
        final var streamConfigurationMapper = Mappers.getMapper(StreamConfigurationMapper.class);

        this.publisher = new VertxPublisher(this, tracerFactory.create(Operation.PUBLISH));
        this.consumer = new VertxConsumer(this, tracerFactory.create(Operation.RECEIVE), consumerConfigurationMapper);
        this.management = new VertxStreamManagement(this, consumerConfigurationMapper, streamConfigurationMapper, this.streamInfoMapper);
    }

    @Override
    public @NonNull Uni<Message> publish(@NonNull final Message message, @NonNull final String stream,
            @NonNull final String subject) {
        return this.publisher.publish(message, stream, subject);
    }

    @Override
    public @NonNull Multi<Message> publish(@NonNull final Multi<Message> messages, @NonNull final String stream,
            @NonNull final String subject) {
        return this.publisher.publish(messages, stream, subject);
    }

    @Override
    public @NonNull Uni<Message> next(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout) {
        return this.consumer.next(stream, consumer, timeout);
    }

    @Override
    public @NonNull Multi<Message> fetch(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout,
            int batchSize) {
        return this.consumer.fetch(stream, consumer, timeout, batchSize);
    }

    @Override
    public @NonNull Uni<MessageInfo> message(@NonNull String stream, long sequence) {
        return this.consumer.message(stream, sequence);
    }

    @Override
    public @NonNull Multi<Message> subscribe(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout,
            int batchSize) {
        return this.consumer.subscribe(stream, consumer, timeout, batchSize);
    }

    @Override
    public @NonNull Uni<ConsumerInfo> consumer(@NonNull String stream, @NonNull String consumer) {
        return this.consumer.consumer(stream, consumer);
    }

    @Override
    public @NonNull Multi<ConsumerInfo> consumers(@NonNull String stream) {
        return this.consumer.consumers(stream);
    }

    @Override
    public @NonNull StreamManagement management() {
        return management;
    }

    @Override
    public @NonNull Uni<StreamInfo> stream(@NonNull final String stream) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.getStreamInfo(stream))))
                .onItem().ifNotNull().transform(streamInfoMapper::map)
                .runSubscriptionOn(this.configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<StreamInfo> streams() {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                .item(Unchecked.supplier(jetStreamManagement::getStreams)))
                .onItem().transformToMulti(streams -> Multi.createFrom().iterable(streams))
                .onItem().transform(streamInfoMapper::map)
                .runSubscriptionOn(this.configuration.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull ObjectStore objectStore(@NonNull final String bucketName) {
        return new VertxObjectStore(bucketName, this);
    }

    @Override
    public @NonNull KeyValue keyValue(@NonNull final String bucketName) {
        return new VertxKeyValue(bucketName, this);
    }

    @NonNull ClientConfiguration configuration() {
        return configuration;
    }

    @NonNull Connection connection() {
        return connection;
    }

    @NonNull Context context() {
        return context;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    private Uni<JetStreamManagement> jetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStreamManagement))
                .map(JetStreamManagement::of);
    }

    private void runOnContext(Runnable action) {
        context.runOnContext(action);
    }
}
