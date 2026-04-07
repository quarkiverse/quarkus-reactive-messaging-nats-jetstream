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
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@Recorder
@RequiredArgsConstructor
public class JetStreamRecorder {
    private final RuntimeValue<ConnectorConfiguration> configuration;

    public void setup() {
        final var client = CDI.current().select(Client.class).get();
        final var streamConfigurationMapper = CDI.current().select(StreamConfigurationMapper.class).get();
        final var pullConsumerConfigurationMapper = CDI.current().select(PullConsumerConfigurationMapper.class).get();
        final var pushConsumerConfigurationMapper = CDI.current().select(PushConsumerConfigurationMapper.class).get();
        final var keyValueStoreConfigurationMapper = CDI.current().select(KeyValueStoreConfigurationMapper.class).get();

        addStreams(streamConfigurationMapper, client);
        addPullConsumers(pullConsumerConfigurationMapper, client);
        addPushConsumers(pushConsumerConfigurationMapper, client);
        addKeyValueStores(keyValueStoreConfigurationMapper, client);
    }

    private void addKeyValueStores(KeyValueStoreConfigurationMapper keyValueStoreConfigurationMapper, Client client) {
        keyValueStoreConfigurationMapper.map(configuration.getValue())
                .forEach(keyValueStoreConfiguration -> client
                        .addKeyValueStoreIfAbsent(keyValueStoreConfiguration)
                        .onFailure()
                        .invoke(failure -> log.errorf(failure, "Failed to configure key value store: %s", failure.getMessage()))
                        .await().indefinitely());
    }

    private void addPushConsumers(PushConsumerConfigurationMapper pushConsumerConfigurationMapper, Client client) {
        pushConsumerConfigurationMapper.map(configuration.getValue())
                .forEach(tuple -> client.addConsumerIfAbsent(tuple.consumerConfiguration(), tuple.pushConfiguration())
                        .onFailure()
                        .invoke(failure -> log.errorf(failure, "Failed to configure push consumer: %s", failure.getMessage()))
                        .await().indefinitely());
    }

    private void addPullConsumers(PullConsumerConfigurationMapper pullConsumerConfigurationMapper, Client client) {
        pullConsumerConfigurationMapper.map(configuration.getValue())
                .forEach(tuple -> client
                        .addConsumerIfAbsent(tuple.consumerConfiguration(), tuple.pullConfiguration())
                        .onFailure()
                        .invoke(failure -> log.errorf(failure, "Failed to configure pull consumer: %s", failure.getMessage()))
                        .await().indefinitely());
    }

    private void addStreams(StreamConfigurationMapper streamConfigurationMapper, Client client) {
        streamConfigurationMapper.map(configuration.getValue())
                .forEach(streamConfiguration -> client.addStreamIfAbsent(streamConfiguration)
                        .onFailure()
                        .invoke(failure -> log.errorf(failure, "Failed to configure streams: %s", failure.getMessage()))
                        .await().indefinitely());
    }
}
