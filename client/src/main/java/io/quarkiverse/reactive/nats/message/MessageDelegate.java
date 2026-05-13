package io.quarkiverse.reactive.nats.message;

import io.quarkiverse.reactive.nats.Context;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

import java.time.Duration;
import java.util.Optional;

public record MessageDelegate(io.nats.client.Message delegate, Context context) implements Message {

    @Override
    public String subject() {
        return delegate.getSubject();
    }

    @Override
    public String replyTo() {
        return delegate.getReplyTo();
    }

    @Override
    public Optional<Headers> headers() {
        return delegate.hasHeaders() ? Optional.of(new HeadersDelegate(delegate.getHeaders())) : Optional.empty();
    }


    @Override
    public Optional<Status> status() {
        return delegate.isStatusMessage() ? Optional.of(new StatusDelegate(delegate.getStatus())) : Optional.empty();
    }

    @Override
    public byte[] data() {
        return delegate.getData();
    }

    @Override
    public String sid() {
        return delegate.getSID();
    }

    @Override
    public Optional<MetaData> metaData() {
        return Optional.ofNullable(delegate.metaData()).map(MetaDataDelegate::new);
    }

    @Override
    public AcknowledgeType lastAcknowledge() {
        return switch (delegate.lastAck()) {
            case AckAck -> AcknowledgeType.AckAck;
            case AckNak -> AcknowledgeType.AckNak;
            case AckNext -> AcknowledgeType.AckNext;
            case AckProgress -> AcknowledgeType.AckProgress;
            case AckTerm -> AcknowledgeType.AckTerm;
        };
    }

    @Override
    public Uni<Void> acknowledge() {
        return Uni.createFrom().item((Unchecked.supplier(() -> {
                    delegate.ack();
                    return null;
                })))
                .emitOn(context::runOnContext)
                .chain(v -> Uni.createFrom().voidItem());
    }

    @Override
    public Uni<Void> acknowledge(Duration timeout) {
        return Uni.createFrom().item((Unchecked.supplier(() -> {
                    delegate.ackSync(timeout);
                    return null;
                })))
                .emitOn(context::runOnContext)
                .chain(v -> Uni.createFrom().voidItem());
    }

    @Override
    public Uni<Void> notAcknowledge() {
        return Uni.createFrom().item((Unchecked.supplier(() -> {
                    delegate.nak();;
                    return null;
                })))
                .emitOn(context::runOnContext)
                .chain(v -> Uni.createFrom().voidItem());
    }

    @Override
    public Uni<Void> notAcknowledgeWithDelay(Duration nakDelay) {
        return Uni.createFrom().item((Unchecked.supplier(() -> {
                    delegate.nakWithDelay(nakDelay);
                    return null;
                })))
                .emitOn(context::runOnContext)
                .chain(v -> Uni.createFrom().voidItem());
    }

    @Override
    public Uni<Void> notAcknowledgeWithDelay(long nakDelayMillis) {
        return Uni.createFrom().item((Unchecked.supplier(() -> {
                    delegate.nakWithDelay(nakDelayMillis);
                    return null;
                })))
                .emitOn(context::runOnContext)
                .chain(v -> Uni.createFrom().voidItem());
    }

    @Override
    public Uni<Void> terminate() {
        return Uni.createFrom().item((Unchecked.supplier(() -> {
                    delegate.term();
                    return null;
                })))
                .emitOn(context::runOnContext)
                .chain(v -> Uni.createFrom().voidItem());
    }

    @Override
    public Uni<Void> inProgress() {
        return Uni.createFrom().item((Unchecked.supplier(() -> {
                    delegate.inProgress();
                    return null;
                })))
                .emitOn(context::runOnContext)
                .chain(v -> Uni.createFrom().voidItem());
    }

    @Override
    public boolean isJetStream() {
        return delegate.isJetStream();
    }
}
