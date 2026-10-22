package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import javax.net.ssl.SSLContext;

record TlsContextRecord(SSLContext sslContext) implements TlsContext {
}
