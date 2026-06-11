package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import io.nats.client.ConsumerContext;

public interface NativeConsumerContext extends ConsumerContext {

    static NativeConsumerContext of(ConsumerContext delegate) {
        return new NativeConsumerContextDelegate(delegate);
    }

}
