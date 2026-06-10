package io.quarkiverse.reactive.messaging.nats.client.stream;

import io.quarkiverse.reactive.messaging.nats.client.api.StreamState;

public interface StreamStateMapper {

    StreamState map(io.nats.client.api.StreamState state);
}
