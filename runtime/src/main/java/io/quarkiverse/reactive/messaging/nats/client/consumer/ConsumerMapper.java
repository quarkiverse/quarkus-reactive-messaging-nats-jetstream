package io.quarkiverse.reactive.messaging.nats.client.consumer;

import io.nats.client.api.ConsumerInfo;
import io.quarkiverse.reactive.messaging.nats.client.api.Consumer;

public interface ConsumerMapper {

    Consumer map(ConsumerInfo consumerInfo);

}
