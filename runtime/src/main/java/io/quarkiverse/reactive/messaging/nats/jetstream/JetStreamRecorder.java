package io.quarkiverse.reactive.messaging.nats.jetstream;

import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.administration.AdministrationConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.administration.JetStreamSetupException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueSetupConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.SetupConfiguration;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class JetStreamRecorder {
    private final static Logger logger = Logger.getLogger(JetStreamRecorder.class);

    private final RuntimeValue<NatsConfiguration> natsConfiguration;
    private final RuntimeValue<JetStreamBuildConfiguration> jetStreamConfiguration;

    public JetStreamRecorder(RuntimeValue<NatsConfiguration> natsConfiguration,
            RuntimeValue<JetStreamBuildConfiguration> jetStreamConfiguration) {
        this.natsConfiguration = natsConfiguration;
        this.jetStreamConfiguration = jetStreamConfiguration;
    }

    public void setupStreams() {
        if (jetStreamConfiguration.getValue().autoConfigure()) {
            try (final var connection = connect()) {
                SetupConfiguration.of(jetStreamConfiguration.getValue())
                        .forEach(setupConfiguration -> connection.addOrUpdateStream(setupConfiguration).await().indefinitely());
                KeyValueSetupConfiguration.of(jetStreamConfiguration.getValue())
                        .forEach(keyValueSetupConfiguration -> connection.addOrUpdateKeyValueStore(keyValueSetupConfiguration)
                                .await().indefinitely());
            } catch (Throwable failure) {
                throw new JetStreamSetupException(String.format("Unable to configure stream: %s", failure.getMessage()),
                        failure);
            }
        }
    }

    private AdministrationConnection connect() {
        final var connectionConfiguration = ConnectionConfiguration.of(natsConfiguration.getValue());
        return new AdministrationConnection(connectionConfiguration,
                (event, message) -> logger.infof("%s with message %s", event, message));
    }
}
