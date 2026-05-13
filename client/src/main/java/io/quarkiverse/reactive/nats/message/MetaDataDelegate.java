package io.quarkiverse.reactive.nats.message;

import io.nats.client.impl.NatsJetStreamMetaData;

import java.time.ZonedDateTime;

public record MetaDataDelegate(NatsJetStreamMetaData delegate) implements MetaData {

    @Override
    public String metaType() {
        return delegate.getMetaType();
    }

    @Override
    public String domain() {
        return delegate.getDomain();
    }

    @Override
    public String stream() {
        return delegate.getStream();
    }

    @Override
    public String consumer() {
        return delegate.getConsumer();
    }

    @Override
    public long deliveredCount() {
        return delegate.deliveredCount();
    }

    @Override
    public long streamSequence() {
        return delegate.streamSequence();
    }

    @Override
    public long consumerSequence() {
        return delegate.consumerSequence();
    }

    @Override
    public ZonedDateTime timestamp() {
        return delegate.timestamp();
    }

    @Override
    public long pendingCount() {
        return delegate.pendingCount();
    }
}
