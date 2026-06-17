package io.quarkiverse.reactive.messaging.nats.client;

import io.quarkiverse.reactive.messaging.nats.client.consumer.ConsumerAware;
import io.quarkiverse.reactive.messaging.nats.client.publisher.PublisherAware;
import io.quarkiverse.reactive.messaging.nats.client.store.KeyValueStoreAware;
import io.quarkiverse.reactive.messaging.nats.client.stream.StreamAware;

public interface Client extends PublisherAware, ConsumerAware, StreamAware, KeyValueStoreAware {



}
