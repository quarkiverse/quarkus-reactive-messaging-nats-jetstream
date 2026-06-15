package io.quarkiverse.reactive.messaging.nats.jetstream;

public interface JetStreamManagement extends io.nats.client.JetStreamManagement {

    static JetStreamManagement of(io.nats.client.JetStreamManagement delegate) {
        return new JetStreamManagementDelegate(delegate);
    }

}
