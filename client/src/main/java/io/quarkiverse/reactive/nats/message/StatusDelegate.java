package io.quarkiverse.reactive.nats.message;

public record StatusDelegate(io.nats.client.support.Status delegate) implements Status {

    @Override
    public int code() {
        return delegate.getCode();
    }

    @Override
    public String message() {
        return delegate.getMessage();
    }
}
