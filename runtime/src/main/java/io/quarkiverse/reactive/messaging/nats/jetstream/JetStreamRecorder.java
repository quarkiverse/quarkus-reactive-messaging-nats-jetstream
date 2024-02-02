package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.time.Duration;

import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.setup.JetStreamSetup;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class JetStreamRecorder {
    private final static Logger logger = Logger.getLogger(JetStreamRecorder.class);

    private final JetStreamSetup jetStreamSetup;
    private final RuntimeValue<NatsConfiguration> natsConfiguration;
    private final RuntimeValue<JetStreamBuildConfiguration> jetStreamConfiguration;

    public JetStreamRecorder(RuntimeValue<NatsConfiguration> natsConfiguration,
            RuntimeValue<JetStreamBuildConfiguration> jetStreamConfiguration) {
        this.jetStreamSetup = new JetStreamSetup();
        this.natsConfiguration = natsConfiguration;
        this.jetStreamConfiguration = jetStreamConfiguration;
    }

    public void setupStreams() {
        logger.info("Setup JetStream");
        try (final var jetStreamClient = new JetStreamClient(ConnectionConfiguration.of(natsConfiguration.getValue()))) {
            final var connection = jetStreamClient.getOrEstablishConnection().await().atMost(Duration.ofSeconds(30));
            jetStreamSetup.setup(connection, jetStreamConfiguration.getValue());
        }
    }
}
