package io.quarkiverse.reactive.nats.consumer;

interface ImperativeDispatcher extends io.nats.client.Dispatcher {

    static ImperativeDispatcher of(io.nats.client.Dispatcher dispatcher) {
        return new ImperativeDispatcherDelegate(dispatcher);
    }

}
