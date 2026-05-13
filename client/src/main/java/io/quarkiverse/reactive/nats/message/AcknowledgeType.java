package io.quarkiverse.reactive.nats.message;

public enum AcknowledgeType {

    // Acknowledgement protocol messages
    AckAck("+ACK", true),
    AckNak("-NAK", true),
    AckProgress("+WPI", false),
    AckTerm("+TERM", true),

    // pull only option
    AckNext("+NXT", false);

    public final String text;
    public final boolean terminal;

    AcknowledgeType(String text, boolean terminal) {
        this.text = text;
        this.terminal = terminal;
    }
}
