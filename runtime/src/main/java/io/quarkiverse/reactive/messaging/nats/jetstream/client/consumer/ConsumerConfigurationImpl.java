package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import lombok.Builder;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Builder
public record ConsumerConfigurationImpl<T>(String name,
                                           String stream,
                                           Boolean durable,
                                           List<String> filterSubjects,
                                           Optional<Duration> ackWait,
                                           DeliverPolicy deliverPolicy,
                                           Optional<Long> startSequence,
                                           Optional<ZonedDateTime> startTime,
                                           Optional<String> description,
                                           Optional<Duration> inactiveThreshold,
                                           Optional<Long> maxAckPending,
                                           Optional<Long> maxDeliver,
                                           ReplayPolicy replayPolicy,
                                           Integer replicas,
                                           Optional<Boolean> memoryStorage,
                                           Optional<String> sampleFrequency,
                                           Map<String, String> metadata,
                                           Optional<List<Duration>> backoff,
                                           Optional<ZonedDateTime> pauseUntil,
                                           Optional<Class<T>> payloadType,
                                           Duration acknowledgeTimeout) implements ConsumerConfiguration<T> {

}
