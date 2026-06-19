package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import io.nats.client.api.AckPolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface ConsumerConfigurationMapper {

    @Mapping(target = "name", source = "name")
    @Mapping(target = "durable", source = "durable")
    @Mapping(target = "filterSubject", source = "filterSubject")
    @Mapping(target = "filterSubjects", source = "filterSubjects")
    @Mapping(target = "acknowledgeWait", source = "ackWait")
    @Mapping(target = "deliverPolicy", source = "deliverPolicy")
    @Mapping(target = "startSequence", source = "startSequence")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "inactiveThreshold", source = "inactiveThreshold")
    @Mapping(target = "maxAcknowledgePending", source = "maxAckPending")
    @Mapping(target = "maxDeliver", source = "maxDeliver")
    @Mapping(target = "replayPolicy", source = "replayPolicy")
    @Mapping(target = "replicas", source = "numReplicas")
    @Mapping(target = "memoryStorage", source = "memStorage")
    @Mapping(target = "sampleFrequency", source = "sampleFrequency")
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "backoff", source = "backoff")
    @Mapping(target = "pauseUntil", source = "pauseUntil")
    @Mapping(target = "headersOnly", source = "headersOnly")
    @Mapping(target = "acknowledgeTimeout", ignore = true)
    @Mapping(target = "pullConfiguration", source = ".")
    ConsumerConfiguration map(io.nats.client.api.ConsumerConfiguration source);

    default io.nats.client.api.ConsumerConfiguration map(final ConsumerConfiguration configuration) {
        var builder = io.nats.client.api.ConsumerConfiguration.builder();
        if (configuration.durable()) {
            builder = builder.durable(configuration.name());
        }
        builder = configuration.filterSubject().map(builder::filterSubject).orElse(builder);
        builder = builder.filterSubjects(configuration.filterSubjects().stream().toList());
        builder = builder.name(configuration.name());
        builder = builder.ackPolicy(AckPolicy.Explicit);
        builder = configuration.acknowledgeWait().map(builder::ackWait).orElse(builder);
        builder = builder.deliverPolicy(map(configuration.deliverPolicy()));
        builder = configuration.startSequence().map(builder::startSequence).orElse(builder);
        builder = configuration.startTime().map(builder::startTime).orElse(builder);
        builder = configuration.description().map(builder::description).orElse(builder);
        builder = configuration.inactiveThreshold().map(builder::inactiveThreshold).orElse(builder);
        builder = configuration.maxAcknowledgePending().map(builder::maxAckPending).orElse(builder);
        builder = configuration.maxDeliver().map(builder::maxDeliver).orElse(builder);
        builder = builder.replayPolicy(map(configuration.replayPolicy()));
        builder = configuration.replicas().map(builder::numReplicas).orElse(builder);
        builder = configuration.memoryStorage().map(builder::memStorage).orElse(builder);
        builder = configuration.sampleFrequency().map(builder::sampleFrequency).orElse(builder);
        if (!configuration.metadata().isEmpty()) {
            builder = builder.metadata(configuration.metadata());
        }
        builder = builder.backoff(configuration.backoff().toArray(new Duration[0]));
        final var pullConfiguration = configuration.pullConfiguration();
        builder = pullConfiguration.maxWaiting().map(builder::maxPullWaiting).orElse(builder);
        builder = pullConfiguration.maxRequestExpires().map(builder::maxExpires).orElse(builder);
        builder = pullConfiguration.maxRequestBatch().map(builder::maxBatch).orElse(builder);
        builder = pullConfiguration.maxRequestMaxBytes().map(builder::maxBytes).orElse(builder);
        return builder.build();
    }

    io.nats.client.api.DeliverPolicy map(DeliverPolicy source);

    io.nats.client.api.ReplayPolicy map(ReplayPolicy source);

    default Optional<Duration> mapDuration(Duration duration) {
        return Optional.ofNullable(duration);
    }

    default Optional<Long> mapLong(Long value) {
        return Optional.ofNullable(value);
    }

    default Optional<Boolean> mapBoolean(Boolean value) {
        return Optional.ofNullable(value);
    }

    default Optional<ZonedDateTime> mapZonedDateTime(ZonedDateTime value) {
        return Optional.ofNullable(value);
    }

    default Optional<Integer> mapInteger(Integer value) {
        return Optional.ofNullable(value);
    }

    default Optional<String> mapString(String value) {
        return Optional.ofNullable(value);
    }

    default Map<String, String> mapMetadata(Map<String, String> metadata) {
        return metadata == null ? Map.of() : metadata;
    }

    default PullConfiguration mapPull(io.nats.client.api.ConsumerConfiguration configuration) {
        if (configuration == null) {
            return null;
        }
        return PullConfiguration.builder()
                .maxWaiting(mapLong(configuration.getMaxPullWaiting()))
                .maxRequestExpires(mapDuration(configuration.getMaxExpires()))
                .maxRequestBatch(mapLong(configuration.getMaxBatch()))
                .maxRequestMaxBytes(mapLong(configuration.getMaxBytes()))
                .build();
    }
}
