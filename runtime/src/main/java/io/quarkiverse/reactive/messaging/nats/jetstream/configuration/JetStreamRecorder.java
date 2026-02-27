package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

import jakarta.enterprise.inject.spi.CDI;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper.KeyValueStoreConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper.PullConsumerConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper.PushConsumerConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper.StreamConfigurationMapper;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import lombok.RequiredArgsConstructor;

@Recorder
@RequiredArgsConstructor
public class JetStreamRecorder {
    private final RuntimeValue<ConnectorConfiguration> configuration;

    public void setup() {
        try {
            final var client = CDI.current().select(Client.class).get();
            final var streamConfigurationMapper = CDI.current().select(StreamConfigurationMapper.class).get();
            final var pullConsumerConfigurationMapper = CDI.current().select(PullConsumerConfigurationMapper.class).get();
            final var pushConsumerConfigurationMapper = CDI.current().select(PushConsumerConfigurationMapper.class).get();
            final var keyValueStoreConfigurationMapper = CDI.current().select(KeyValueStoreConfigurationMapper.class).get();

            streamConfigurationMapper.map(configuration.getValue())
                    .forEach(streamConfiguration -> client.addStreamIfAbsent(streamConfiguration).await().indefinitely());

            pullConsumerConfigurationMapper.map(configuration.getValue())
                    .forEach(tuple -> client
                            .addConsumerIfAbsent(tuple.consumerConfiguration(), tuple.pullConfiguration()).await()
                            .indefinitely());

            pushConsumerConfigurationMapper.map(configuration.getValue())
                    .forEach(tuple -> client.addConsumerIfAbsent(tuple.consumerConfiguration(), tuple.pushConfiguration())
                            .await()
                            .indefinitely());

            keyValueStoreConfigurationMapper.map(configuration.getValue())
                    .forEach(keyValueStoreConfiguration -> client
                            .addKeyValueStoreIfAbsent(keyValueStoreConfiguration).await().indefinitely());
        } catch (Exception failure) {
            throw new RuntimeException(
                    String.format("Unable to configure streams and key value stores: %s", failure.getMessage()),
                    failure);
        }
    }
}
