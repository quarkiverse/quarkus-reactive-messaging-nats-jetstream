package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import jakarta.enterprise.inject.spi.CDI;

import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.JetStreamConfiguration;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class JetStreamRecorder {
    private final RuntimeValue<JetStreamConfiguration> natsConfiguration;

    public JetStreamRecorder(RuntimeValue<JetStreamConfiguration> natsConfiguration) {
        this.natsConfiguration = natsConfiguration;
    }

    public void setup() {
        if (natsConfiguration.getValue().autoConfiguration()) {
            try  {
                final var client = CDI.current().select(Client.class).get();
                natsConfiguration.getValue().streams()
                        .forEach((key, value) -> client.withStreamContext(streamContext -> streamContext.addIfAbsent(key, value)).await().indefinitely());
                natsConfiguration.getValue().keyValueStores()
                                .forEach((key, value) -> client.withKeyValueStoreContext(keyValueStoreContext -> keyValueStoreContext.addIfAbsent(key, value)).await().indefinitely());
            } catch (Exception failure) {
                throw new RuntimeException(
                        String.format("Unable to configure streams and key value stores: %s", failure.getMessage()),
                        failure);
            }
        }
    }
}
