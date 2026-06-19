package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import io.nats.client.JetStreamSubscription;

public interface Subscription extends JetStreamSubscription {

    static Subscription of(JetStreamSubscription subscription) {
        return new SubscriptionDelegate(subscription);
    }

}
