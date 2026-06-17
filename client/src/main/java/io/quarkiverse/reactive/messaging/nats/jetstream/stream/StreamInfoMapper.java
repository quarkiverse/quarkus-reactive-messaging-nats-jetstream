package io.quarkiverse.reactive.messaging.nats.jetstream.stream;

import org.mapstruct.Mapper;

@Mapper(uses = { StreamConfigurationMapper.class })
public interface StreamInfoMapper {

    StreamInfo map(io.nats.client.api.StreamInfo source);

}
