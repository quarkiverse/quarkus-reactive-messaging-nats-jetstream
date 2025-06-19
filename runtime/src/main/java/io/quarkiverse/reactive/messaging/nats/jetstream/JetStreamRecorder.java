package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.DefaultConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.SetupException;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import jakarta.enterprise.inject.spi.CDI;

@Recorder
public class JetStreamRecorder {
    private final RuntimeValue<NatsConfiguration> natsConfiguration;

    public JetStreamRecorder(RuntimeValue<NatsConfiguration> natsConfiguration) {
        this.natsConfiguration = natsConfiguration;
    }

    public void setup() {
        if (natsConfiguration.getValue().autoConfigure()) {
            final var connectionFactory = CDI.current().select(ConnectionFactory.class).get();
            try (final var connection = connectionFactory.create(natsConfiguration.getValue().connection(), new DefaultConnectionListener())
                    .await().indefinitely()) {
                natsConfiguration.getValue().jetStream().map(NatsConfiguration.JetStream::streams).ifPresent(streamConfigurations -> {
                    connection.streamManagement()
                            .onItem()
                            .transformToUni(streamManagement -> streamManagement
                                    .addStreams(streamConfigurations))
                            .await().indefinitely();
                });

                connection.keyValueStoreManagement()
                        .onItem()
                        .transformToUni(keyValueStoreManagement -> keyValueStoreManagement
                                .addKeyValueStores(natsConfiguration.getValue().keyValueStores()))
                        .await().indefinitely();
            } catch (Exception failure) {
                throw new SetupException(
                        String.format("Unable to configure streams and key value stores: %s", failure.getMessage()),
                        failure);
            }
        }
    }
}
