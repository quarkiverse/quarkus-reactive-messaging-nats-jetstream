package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerContextAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher.PublisherAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamContextAware;

public interface Client extends ConsumerContextAware, PublisherAware, KeyValueStoreAware, StreamContextAware {

}
