package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import java.util.Optional;

import javax.net.ssl.SSLContext;

public interface TlsContext {

    Optional<SSLContext> sslContext();

}
