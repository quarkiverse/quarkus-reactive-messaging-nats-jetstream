package io.quarkiverse.reactive.messaging.nats.jetstream.reply;

/**
 * Raised when the reply consumer cannot be created for a request/reply channel.
 */
public class JetStreamRequestSubscriptionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String channelName;
    private final String replySubject;

    public JetStreamRequestSubscriptionException(String channelName, String replySubject, Throwable cause) {
        super("Failed to create reply consumer for channel '" + channelName + "' on subject '" + replySubject + "'", cause);
        this.channelName = channelName;
        this.replySubject = replySubject;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getReplySubject() {
        return replySubject;
    }
}
