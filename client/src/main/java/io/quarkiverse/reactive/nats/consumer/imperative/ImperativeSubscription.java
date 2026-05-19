package io.quarkiverse.reactive.nats.consumer.imperative;

public interface ImperativeSubscription extends io.nats.client.Subscription {

    static ImperativeSubscription of(io.nats.client.Subscription subscription) {
        return new ImperativeSubscriptionDelegate(subscription);
    }

}
