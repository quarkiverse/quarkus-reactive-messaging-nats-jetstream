package io.quarkiverse.reactive.nats.consumer.imperative;

public interface ImperativeConsumerContext extends io.nats.client.ConsumerContext {
    static ImperativeConsumerContext of(io.nats.client.ConsumerContext consumerContext) {
        return new ImperativeConsumerContextDelegate(consumerContext);
    }
}
