package io.quarkiverse.reactive.nats.jetstream.connection;

public interface NativeConnectionFactory {

    static NativeConnectionFactory of() {
        return new DefaultNativeConnectionFactory();
    }

    NativeConnection create(ConnectionConfiguration configuration);

}
