package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
record ObjectEntryImpl(byte @NonNull [] data,
        @NonNull ObjectInfo info) implements ObjectEntry {
}
