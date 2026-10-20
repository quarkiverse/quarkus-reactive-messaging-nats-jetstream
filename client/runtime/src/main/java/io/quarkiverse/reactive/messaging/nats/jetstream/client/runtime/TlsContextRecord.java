package io.quarkiverse.reactive.messaging.nats.jetstream.client.runtime;

import javax.net.ssl.SSLContext;

record TlsContextRecord(SSLContext sslContext) implements TlsContext {
}
