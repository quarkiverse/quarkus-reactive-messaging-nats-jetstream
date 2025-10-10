package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

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
    private final RuntimeValue<Client> client;
    private final RuntimeValue<StreamConfigurationMapper> connectorConfigurationMapper;
    private final RuntimeValue<KeyValueStoreConfigurationMapper> keyValueStoreConfigurationMapper;
    private final RuntimeValue<PullConsumerConfigurationMapper> pullConsumerConfigurationMapper;
    private final RuntimeValue<PushConsumerConfigurationMapper> pushConsumerConfigurationMapper;

    public void setup() {
        if (configuration.getValue().autoConfiguration()) {
            try {
                connectorConfigurationMapper.getValue().map(configuration.getValue())
                        .forEach(client.getValue()::addStreamIfAbsent);

                pullConsumerConfigurationMapper.getValue().map(configuration.getValue())
                        .forEach(tuple -> client.getValue()
                                .addConsumerIfAbsent(tuple.consumerConfiguration(), tuple.pullConfiguration()).await()
                                .indefinitely());

                pushConsumerConfigurationMapper.getValue().map(configuration.getValue())
                        .forEach(tuple -> client.getValue()
                                .addConsumerIfAbsent(tuple.consumerConfiguration(), tuple.pushConfiguration()).await()
                                .indefinitely());

                keyValueStoreConfigurationMapper.getValue().map(configuration.getValue())
                        .forEach(keyValueStoreConfiguration -> client.getValue()
                                .addKeyValueStoreIfAbsent(keyValueStoreConfiguration).await().indefinitely());
            } catch (Exception failure) {
                throw new RuntimeException(
                        String.format("Unable to configure streams and key value stores: %s", failure.getMessage()),
                        failure);
            }
        }
    }
}
