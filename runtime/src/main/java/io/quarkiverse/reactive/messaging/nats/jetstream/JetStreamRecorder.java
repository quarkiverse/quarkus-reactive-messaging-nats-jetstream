package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.time.Duration;

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
    private final static Duration WAIT = Duration.ofSeconds(10);

    private final RuntimeValue<NatsConfiguration> natsConfiguration;
    private final RuntimeValue<JetStreamBuildConfiguration> jetStreamConfiguration;

    public JetStreamRecorder(RuntimeValue<NatsConfiguration> natsConfiguration,
            RuntimeValue<JetStreamBuildConfiguration> jetStreamConfiguration) {
        this.natsConfiguration = natsConfiguration;
        this.jetStreamConfiguration = jetStreamConfiguration;
    }

    public void setup() {
        if (jetStreamConfiguration.getValue().autoConfigure()) {
            final var connectionConfiguration = ConnectionConfiguration.of(natsConfiguration.getValue());
            final var connectionFactory = CDI.current().select(ConnectionFactory.class).get();
            try (final var connection = connectionFactory.create(connectionConfiguration, new DefaultConnectionListener())
                    .await().atMost(WAIT)) {
                connection.addStreams(StreamSetupConfiguration.of(jetStreamConfiguration.getValue())).await().atMost(WAIT);
                connection.addOrUpdateKeyValueStores(KeyValueSetupConfiguration.of(jetStreamConfiguration.getValue())).await()
                        .atMost(WAIT);
            } catch (Throwable failure) {
                throw new SetupException(
                        String.format("Unable to configure streams and key value stores: %s", failure.getMessage()),
                        failure);
            }
        }
    }
}
