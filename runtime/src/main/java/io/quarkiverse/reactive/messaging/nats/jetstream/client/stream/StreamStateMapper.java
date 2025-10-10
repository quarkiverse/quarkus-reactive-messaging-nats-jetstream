package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamState;

public interface StreamStateMapper {

    StreamState map(io.nats.client.api.StreamState state);
}
