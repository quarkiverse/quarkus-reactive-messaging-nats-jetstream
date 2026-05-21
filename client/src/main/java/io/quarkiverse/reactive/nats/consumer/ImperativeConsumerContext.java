package io.quarkiverse.reactive.nats.consumer;

interface ImperativeConsumerContext extends io.nats.client.ConsumerContext {
    static ImperativeConsumerContext of(io.nats.client.ConsumerContext consumerContext) {
        return new ImperativeConsumerContextDelegate(consumerContext);
    }
}
