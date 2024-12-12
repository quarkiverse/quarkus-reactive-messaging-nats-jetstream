package io.quarkiverse.reactive.messaging.nats.jetstream;

import jakarta.enterprise.inject.spi.CDI;

import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.DefaultConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.SetupException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamSetupConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueSetupConfiguration;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class JetStreamRecorder {
    private final RuntimeValue<NatsConfiguration> natsConfiguration;
    private final RuntimeValue<JetStreamConfiguration> jetStreamConfiguration;

    public JetStreamRecorder(RuntimeValue<NatsConfiguration> natsConfiguration,
            RuntimeValue<JetStreamConfiguration> jetStreamConfiguration) {
        this.natsConfiguration = natsConfiguration;
        this.jetStreamConfiguration = jetStreamConfiguration;
    }

    public void setup() {
        if (jetStreamConfiguration.getValue().autoConfigure()) {
            final var connectionConfiguration = ConnectionConfiguration.of(natsConfiguration.getValue());
            final var connectionFactory = CDI.current().select(ConnectionFactory.class).get();
            try (final var connection = connectionFactory.create(connectionConfiguration, new DefaultConnectionListener())
                    .await().indefinitely()) {
                connection.streamManagement()
                        .onItem()
                        .transformToUni(streamManagement -> streamManagement
                                .addStreams(StreamSetupConfiguration.of(jetStreamConfiguration.getValue())))
                        .await().indefinitely();
                connection.keyValueStoreManagement()
                        .onItem()
                        .transformToUni(keyValueStoreManagement -> keyValueStoreManagement
                                .addKeyValueStores(KeyValueSetupConfiguration.of(jetStreamConfiguration.getValue())))
                        .await().indefinitely();
            } catch (Exception failure) {
                throw new SetupException(
                        String.format("Unable to configure streams and key value stores: %s", failure.getMessage()),
                        failure);
            }
        }
    }
}
