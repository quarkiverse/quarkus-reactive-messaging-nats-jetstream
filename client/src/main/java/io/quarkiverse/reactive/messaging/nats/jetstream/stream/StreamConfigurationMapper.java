package io.quarkiverse.reactive.messaging.nats.jetstream.stream;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;

@Mapper
public interface StreamConfigurationMapper {

    @Mapping(target = "maxMessagesPerSubject", source = "maxMsgsPerSubject")
    @Mapping(target = "maxMessages", source = "maxMsgs")
    @Mapping(target = "maximumMessageSize", source = "maxMsgSize")
    @Mapping(target = "allowMessageSchedules", source = "allowMsgSchedules")
    StreamConfiguration map(io.nats.client.api.StreamConfiguration source);

    @Mapping(target = "maxMsgSize", source = "maximumMessageSize")
    io.nats.client.api.StreamConfiguration map(StreamConfiguration source);

    default long map(Duration value) {
        return value.toNanos();
    }

    default io.nats.client.api.Mirror map(Mirror value) {
        return io.nats.client.api.Mirror.builder()
                .name(value.name())
                .consumerSource(map(value.consumerSource()))
                .external(map(value.external()))
                .filterSubject(value.filterSubject())
                .startSeq(value.startSeq())
                .startTime(value.startTime())
                .subjectTransforms(value.subjectTransforms().stream().map(this::map).toList())
                .build();
    }

    io.nats.client.api.ConsumerSource map(ConsumerSource value);

    io.nats.client.api.External map(External value);

    io.nats.client.api.SubjectTransform map(SubjectTransform value);

    io.nats.client.api.Source map(Source value);
}
