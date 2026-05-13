package io.quarkiverse.reactive.nats.message;

/**
 * @see io.nats.client.MessageHandler
 */
public interface MessageHandler {

    /**
     * @see io.nats.client.MessageHandler#onMessage(io.nats.client.Message)
     */
    void onMessage(Message message) throws InterruptedException;

}
