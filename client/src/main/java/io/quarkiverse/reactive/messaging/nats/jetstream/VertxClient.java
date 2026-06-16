package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.time.Duration;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.ConsumerInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.MessageInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing.Operation;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing.TracerFactory;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
class VertxClient implements Client {
    private final Connection connection;
    private final Publisher publisher;
    private final Consumer consumer;
    private final StreamManagement management;

    public VertxClient(ClientConfiguration configuration, Connection connection, Context context, TracerFactory tracerFactory) {
        this.connection = connection;
        this.publisher = new VertxPublisher(configuration, connection, context, tracerFactory.create(Operation.PUBLISH));
        this.consumer = new VertxConsumer(configuration, connection, context, tracerFactory.create(Operation.RECEIVE));
        this.management = new VertxStreamManagement(configuration, connection, context, this);
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
    public StreamManagement management() {
        return management;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
