package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher.PublisherAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamAware;

public interface Client extends PublisherAware, ConsumerAware, StreamAware, KeyValueStoreAware {

}
