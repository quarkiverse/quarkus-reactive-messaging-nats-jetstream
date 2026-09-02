package io.quarkiverse.reactive.messaging.nats.jetstream.connector.client;

import javax.net.ssl.SSLContext;

record TlsContextRecord(SSLContext sslContext) implements TlsContext {
}
