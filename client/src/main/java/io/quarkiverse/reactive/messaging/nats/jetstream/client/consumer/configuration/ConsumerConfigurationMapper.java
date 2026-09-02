package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import io.nats.client.api.AckPolicy;

@Mapper(uses = { OptionalMapper.class, PullOptionsMapper.class, PushOptionsMapper.class })
interface ConsumerConfigurationMapper {
    String ACKNOWLEDGE_TIMEOUT = "acknowledgeTimeout";

    @Mapping(target = "name", source = "name")
    @Mapping(target = "durable", expression = "java(source.getDurable() != null)")
    @Mapping(target = "filterSubject", expression = "java(Optional.ofNullable(source.getFilterSubject()))")
    @Mapping(target = "filterSubjects", expression = "java(source.getFilterSubjects() != null ? java.util.Optional.of(source.getFilterSubjects().stream().collect(java.util.stream.Collectors.toSet())) : java.util.Optional.empty())")
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
    @Mapping(target = "backoff", expression = "java(source.getBackoff() != null ? java.util.Optional.of(source.getBackoff()) : java.util.Optional.empty())")
    @Mapping(target = "pauseUntil", source = "pauseUntil")
    @Mapping(target = "headersOnly", source = "headersOnly")
    @Mapping(target = "acknowledgeTimeout", expression = "java(acknowledgeTimeout(source))")
    @Mapping(target = "metadata", expression = "java(Optional.ofNullable(source.getMetadata()).orElseGet(java.util.Map::of))")
    @Mapping(target = "pullOptions", expression = "java(pullOptions(source))")
    @Mapping(target = "pushOptions", expression = "java(pushOptions(source))")
    ConsumerConfigurationImpl map(io.nats.client.api.ConsumerConfiguration source);

    default io.nats.client.api.@NonNull ConsumerConfiguration map(@NonNull final ConsumerConfiguration consumerConfiguration) {
        final var deliverPolicyMapper = Mappers.getMapper(DeliverPolicyMapper.class);
        final var replayPolicyMapper = Mappers.getMapper(ReplayPolicyMapper.class);

        var builder = io.nats.client.api.ConsumerConfiguration.builder();
        if (consumerConfiguration.durable()) {
            builder = builder.durable(consumerConfiguration.name());
        }
        if (consumerConfiguration.filterSubjects().isPresent() && !consumerConfiguration.filterSubjects().get().isEmpty()) {
            builder = builder.filterSubjects(consumerConfiguration.filterSubjects().get().stream().toList());
        } else {
            builder = consumerConfiguration.filterSubject().map(builder::filterSubject).orElse(builder);
        }
        builder = builder.name(consumerConfiguration.name());
        builder = builder.ackPolicy(AckPolicy.Explicit);
        builder = consumerConfiguration.acknowledgeWait().map(builder::ackWait).orElse(builder);
        builder = builder.deliverPolicy(deliverPolicyMapper.map(consumerConfiguration.deliverPolicy()));
        builder = builder.startSequence(consumerConfiguration.startSequence());
        builder = consumerConfiguration.startTime().map(builder::startTime).orElse(builder);
        builder = consumerConfiguration.description().map(builder::description).orElse(builder);
        builder = consumerConfiguration.inactiveThreshold().map(builder::inactiveThreshold).orElse(builder);
        builder = consumerConfiguration.maxAcknowledgePending().map(builder::maxAckPending).orElse(builder);
        builder = consumerConfiguration.maxDeliver().map(builder::maxDeliver).orElse(builder);
        builder = builder.replayPolicy(replayPolicyMapper.map(consumerConfiguration.replayPolicy()));
        builder = consumerConfiguration.replicas().map(builder::numReplicas).orElse(builder);
        builder = builder.memStorage(consumerConfiguration.memoryStorage());
        builder = consumerConfiguration.sampleFrequency().map(builder::sampleFrequency).orElse(builder);
        if (!consumerConfiguration.metadata().isEmpty()) {
            builder = builder.metadata(consumerConfiguration.metadata());
        }
        if (consumerConfiguration.backoff().isPresent() && !consumerConfiguration.backoff().get().isEmpty()) {
            builder = builder.backoff(consumerConfiguration.backoff().get().toArray(new Duration[0]));
        }
        builder = consumerConfiguration.pauseUntil().map(builder::pauseUntil).orElse(builder);
        builder = consumerConfiguration.pullOptions().flatMap(PullOptions::maxWaiting).map(builder::maxPullWaiting)
                .orElse(builder);
        builder = consumerConfiguration.pullOptions().flatMap(PullOptions::maxExpires).map(builder::maxExpires).orElse(builder);
        builder = consumerConfiguration.pullOptions().flatMap(PullOptions::maxBatch).map(builder::maxBatch).orElse(builder);
        builder = consumerConfiguration.pullOptions().flatMap(PullOptions::maxBytes).map(builder::maxBytes).orElse(builder);

        builder = consumerConfiguration.pushOptions().map(PushOptions::deliverSubject).map(builder::deliverSubject)
                .orElse(builder);
        builder = consumerConfiguration.pushOptions().map(PushOptions::flowControl)
                .flatMap(flowControl -> flowControl(flowControl, consumerConfiguration)).map(builder::flowControl)
                .orElse(builder);
        builder = consumerConfiguration.pushOptions().flatMap(PushOptions::idleHeartbeat).map(builder::idleHeartbeat)
                .orElse(builder);
        builder = consumerConfiguration.pushOptions().flatMap(PushOptions::rateLimit).map(builder::rateLimit).orElse(builder);
        builder = consumerConfiguration.pushOptions().flatMap(PushOptions::deliverGroup).map(builder::deliverGroup)
                .orElse(builder);

        Map<String, String> metadata = new java.util.HashMap<>(consumerConfiguration.metadata());
        metadata.put(ACKNOWLEDGE_TIMEOUT, String.valueOf(consumerConfiguration.acknowledgeTimeout().toNanos()));
        builder = builder.metadata(metadata);

        return builder.build();
    }

    @SuppressWarnings("OptionalOfNullableMisuse")
    default Duration acknowledgeTimeout(io.nats.client.api.ConsumerConfiguration configuration) {
        return Optional.ofNullable(configuration.getMetadata())
                .map(m -> m.get(ACKNOWLEDGE_TIMEOUT))
                .map(Long::parseLong)
                .map(Duration::ofNanos)
                .orElse(Duration.ofSeconds(10));
    }

    default Optional<PullOptions> pullOptions(io.nats.client.api.ConsumerConfiguration configuration) {
        final var mapper = Mappers.getMapper(PullOptionsMapper.class);
        final var pullOptions = mapper.map(configuration);
        if (pullOptions.maxBatch().isPresent()
                || pullOptions.maxExpires().isPresent()
                || pullOptions.maxBytes().isPresent()
                || pullOptions.maxWaiting().isPresent()) {
            return Optional.of(pullOptions);
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("ConstantValue")
    default Optional<PushOptions> pushOptions(io.nats.client.api.ConsumerConfiguration configuration) {
        final var mapper = Mappers.getMapper(PushOptionsMapper.class);
        final var pushOptions = mapper.map(configuration);
        if (pushOptions.deliverSubject() != null) {
            return Optional.of(pushOptions);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Duration> flowControl(boolean flowControl,
            ConsumerConfiguration consumerConfiguration) {
        if (flowControl) {
            return Optional.of(consumerConfiguration.pushOptions().flatMap(PushOptions::idleHeartbeat).orElseThrow(
                    () -> new IllegalArgumentException("Idle heartbeat must be set when flow control is enabled")));
        }
        return Optional.empty();
    }
}
