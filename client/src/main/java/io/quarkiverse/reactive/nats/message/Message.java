package io.quarkiverse.reactive.nats.message;

import io.quarkiverse.reactive.nats.Client;
import io.quarkiverse.reactive.nats.Context;
import io.quarkiverse.reactive.nats.consumer.Subscription;
import io.quarkiverse.reactive.nats.message.imperative.ImperativeMessage;
import io.smallrye.mutiny.Uni;

import java.time.Duration;
import java.util.Optional;

public interface Message  {

    static Message of(ImperativeMessage message, Context context) {
        return new MessageDelegate(message, context);
    }


    /**
     * the subject that this message was sent to
     * @return the subject
     */
    String subject();

    /**
     * the subject the application is expected to send a reply message on
     * @return the reply to
     */
    String replyTo();


    /**
     * the headers object for the message
     * @return the headers
     */
    Optional<Headers> headers();

    /**
     * the status object message if this is a status message
     * @return the status object
     */
    Optional<Status> status();

    /**
     * the data from the message
     * @return the data
     */
    byte[] data();

    /**
     * the id associated with the subscription, used by the connection when processing an incoming
     * message from the server
     * @return the SID
     */
    String sid();

    /**
     * Gets the metadata associated with a JetStream message.
     * metadata or empty if the message is not a JetStream message.
     * @return the metadata
     */
    Optional<MetaData> metaData();

    /**
     * the last ack that was done with this message
     * the last ack or null
     * @return the last ack
     */
    AcknowledgeType lastAcknowledge();

    /**
     * ack acknowledges a JetStream messages received from a Consumer, indicating the message
     * should not be received again later.
     */
    Uni<Void> acknowledge();

    /**
     * acknowledge acknowledges a JetStream messages received from a Consumer, indicating the message
     * should not be received again later.  Duration.ZERO does not confirm the acknowledgement.
     * @param timeout the duration to wait for an ack confirmation
     */
    Uni<Void> acknowledge(Duration timeout);

    /**
     * nak acknowledges a JetStream message has been received but indicates that the message
     * is not completely processed and should be sent again later.
     */
    Uni<Void> notAcknowledge();

    /**
     * nak acknowledges a JetStream message has been received but indicates that the message
     * is not completely processed and should be sent again later, after at least the delay amount.
     * @param nakDelay tell the server how long to delay before processing the ack
     */
    Uni<Void> notAcknowledgeWithDelay(Duration nakDelay);

    /**
     * nak acknowledges a JetStream message has been received but indicates that the message
     * is not completely processed and should be sent again later, after at least the delay amount.
     * @param nakDelayMillis tell the server how long to delay before processing the ack
     */
    Uni<Void> notAcknowledgeWithDelay(long nakDelayMillis);

    /**
     * term instructs the server to stop redelivery of this message without acknowledging it as
     * successfully processed.
     */
    Uni<Void> terminate();

    /**
     *  Indicates that this message is being worked on and reset redelivery timer in the server.
     */
    Uni<Void> inProgress();

    /**
     * Checks if a message is from JetStream or is a standard message.
     * @return true if the message is from JetStream.
     */
    boolean isJetStream();

    /**
     * The number of bytes the server counts for the message when calculating byte counts.
     * Only applies to JetStream messages received from the server.
     * @return the consumption byte count or -1 if the message implementation does not support this method
     */
    default long consumeByteCount() { return -1; }

}
