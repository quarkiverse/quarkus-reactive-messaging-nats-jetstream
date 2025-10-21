package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

public interface StreamConfigurationMapper {

    io.nats.client.api.StreamConfiguration map(StreamConfiguration streamConfiguration);

    StreamConfiguration map(io.nats.client.api.StreamConfiguration configuration);
}
