package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerContextConsumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.DefaultConsumerContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher.DefaultPublisherContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher.PublisherContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher.PublisherContextConsumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.DefaultKeyValueStoreContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreContextConsumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.DefaultStreamContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamContextConsumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultClient implements Client {
    private final StreamContext streamContext;
    private final KeyValueStoreContext keyValueStoreContext;
    private final PublisherContext publisherContext;
    private final ConsumerContext consumerContext;

    DefaultClient(final ConnectionFactory connectionFactory,
                  final MessageMapper messageMapper,
                  final PayloadMapper payloadMapper,
                  final ConsumerMapper consumerMapper,
                  final StreamStateMapper streamStateMapper,
                  final StreamConfigurationMapper streamConfigurationMapper,
                  final KeyValueConfigurationMapper keyValueConfigurationMapper,
                  final ConsumerConfigurationMapper consumerConfigurationMapper,
                  final TracerFactory tracerFactory,
                  final ExecutionHolder executionHolder) {
        this.streamContext = new DefaultStreamContext(executionHolder, connectionFactory, streamStateMapper, streamConfigurationMapper);
        this.keyValueStoreContext = new DefaultKeyValueStoreContext(executionHolder, connectionFactory, payloadMapper, keyValueConfigurationMapper);
        this.publisherContext = new DefaultPublisherContext(executionHolder, connectionFactory, tracerFactory, payloadMapper);
        this.consumerContext = new DefaultConsumerContext(executionHolder, connectionFactory, consumerConfigurationMapper, consumerMapper, tracerFactory, messageMapper, payloadMapper);
    }

    @Override
    public <T> T withConsumerContext(ConsumerContextConsumer<T> consumer) {
        return consumer.accept(consumerContext);
    }

    @Override
    public <T> T withPublisherContext(PublisherContextConsumer<T> consumer) {
        return consumer.accept(publisherContext);
    }

    @Override
    public <T> T withKeyValueStoreContext(KeyValueStoreContextConsumer<T> consumer) {
        return consumer.accept(keyValueStoreContext);
    }

    @Override
    public <T> T withStreamContext(StreamContextConsumer<T> consumer) {
        return consumer.accept(streamContext);
    }

}
