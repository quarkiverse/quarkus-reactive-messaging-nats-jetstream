package io.quarkiverse.reactive.messaging.nats.jetstream.client;

/**
 * The JetStreamManagement interface provides methods for managing streams, consumers,
 * and messages in a JetStream context. It extends the core NATS JetStreamManagement
 * functionality, offering tools for interacting with the backing JetStream storage and
 * configuration.
 */
public interface NativeJetStreamManagement extends io.nats.client.JetStreamManagement {

    /**
     * Creates an instance of JetStreamManagement from the provided delegate.
     *
     * @param delegate the underlying JetStreamManagement instance to delegate calls to
     * @return a new instance of JetStreamManagement that wraps the given delegate
     */
    static NativeJetStreamManagement of(io.nats.client.JetStreamManagement delegate) {
        return new NativeJetStreamManagementDelegate(delegate);
    }

}
