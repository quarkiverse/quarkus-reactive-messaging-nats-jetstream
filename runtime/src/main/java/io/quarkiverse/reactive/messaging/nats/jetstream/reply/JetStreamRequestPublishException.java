package io.quarkiverse.reactive.messaging.nats.jetstream.reply;

/**
 * Raised when a request fails to publish on the request channel.
 */
public class JetStreamRequestPublishException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String channelName;

    public JetStreamRequestPublishException(String channelName, Throwable cause) {
        super("Failed to publish request on channel '" + channelName + "'", cause);
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }
}
