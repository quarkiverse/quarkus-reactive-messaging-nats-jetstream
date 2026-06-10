package io.quarkiverse.reactive.messaging.nats.client.connection;

import java.util.Optional;

import javax.net.ssl.SSLContext;

public interface TlsContext {

    Optional<SSLContext> sslContext();

}
