package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import io.nats.client.JetStreamSubscription;

public interface NativeSubscription extends JetStreamSubscription {

    static NativeSubscription of(JetStreamSubscription subscription) {
        return new NativeSubscriptionDelegate(subscription);
    }

}
