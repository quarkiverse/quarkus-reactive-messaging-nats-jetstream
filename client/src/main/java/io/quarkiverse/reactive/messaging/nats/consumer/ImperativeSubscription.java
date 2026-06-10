package io.quarkiverse.reactive.messaging.nats.consumer;

interface ImperativeSubscription extends io.nats.client.Subscription {

    static ImperativeSubscription of(io.nats.client.Subscription subscription) {
        return new ImperativeSubscriptionDelegate(subscription);
    }

}
