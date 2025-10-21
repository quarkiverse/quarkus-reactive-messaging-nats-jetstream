package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import io.nats.client.api.ConsumerInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;

public interface ConsumerMapper {

    Consumer map(ConsumerInfo consumerInfo);

}
