package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import java.util.Optional;

import javax.net.ssl.SSLContext;

record TlsContextImpl(Optional<SSLContext> sslContext) implements TlsContext {
}
