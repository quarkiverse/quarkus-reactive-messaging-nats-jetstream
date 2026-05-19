package io.quarkiverse.reactive.nats.consumer.imperative;

public interface ImperativeDispatcher extends io.nats.client.Dispatcher {

    static ImperativeDispatcher of(io.nats.client.Dispatcher dispatcher) {
        return new ImperativeDispatcherDelegate(dispatcher);
    }

}
