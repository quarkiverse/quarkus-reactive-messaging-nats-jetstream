package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.Priority;
import org.eclipse.microprofile.config.spi.Converter;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerType;
import io.smallrye.config.common.utils.StringUtil;

@Priority(100)
public class ConsumerConfigurationConverter implements Converter<ConsumerConfiguration> {

    @Override
    public ConsumerConfiguration convert(String value) throws IllegalArgumentException, NullPointerException {
        if (value.isEmpty()) {
            return null;
        } else {
            String[] itemStrings = StringUtil.split(value);

            return new ConsumerConfiguration() {
                @Override
                public ConsumerType type() {
                    return null;
                }

                @Override
                public Boolean durable() {
                    return null;
                }

                @Override
                public String subject() {
                    return "";
                }

                @Override
                public Optional<Duration> ackWait() {
                    return Optional.empty();
                }

                @Override
                public DeliverPolicy deliverPolicy() {
                    return null;
                }

                @Override
                public Optional<Long> startSequence() {
                    return Optional.empty();
                }

                @Override
                public Optional<ZonedDateTime> startTime() {
                    return Optional.empty();
                }

                @Override
                public Optional<String> description() {
                    return Optional.empty();
                }

                @Override
                public Optional<Duration> inactiveThreshold() {
                    return Optional.empty();
                }

                @Override
                public Optional<Long> maxAckPending() {
                    return Optional.empty();
                }

                @Override
                public Optional<Long> maxDeliver() {
                    return Optional.empty();
                }

                @Override
                public ReplayPolicy replayPolicy() {
                    return null;
                }

                @Override
                public Integer replicas() {
                    return 0;
                }

                @Override
                public Optional<Boolean> memoryStorage() {
                    return Optional.empty();
                }

                @Override
                public Optional<String> sampleFrequency() {
                    return Optional.empty();
                }

                @Override
                public Map<String, String> metadata() {
                    return Map.of();
                }

                @Override
                public Optional<List<Duration>> backoff() {
                    return Optional.empty();
                }

                @Override
                public Optional<ZonedDateTime> pauseUntil() {
                    return Optional.empty();
                }

                @Override
                public Optional<Class<?>> payloadType() {
                    return Optional.empty();
                }

                @Override
                public Optional<Duration> acknowledgeTimeout() {
                    return Optional.empty();
                }
            };
        }
    }
}
