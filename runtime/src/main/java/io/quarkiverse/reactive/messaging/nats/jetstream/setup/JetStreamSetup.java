package io.quarkiverse.reactive.messaging.nats.jetstream.setup;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfoOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamBuildConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;

public class JetStreamSetup {
    private static final Logger logger = Logger.getLogger(JetStreamSetup.class);

    public void setup(Connection connection,
            JetStreamBuildConfiguration jetStreamConfiguration) {
        try {
            if (jetStreamConfiguration.autoConfigure()) {
                final var setupConfigurations = JetStreamSetupConfiguration.of(jetStreamConfiguration);
                final var jsm = connection.jetStreamManagement();
                final var streamNames = jsm.getStreamNames();
                setupConfigurations
                        .forEach(setupConfiguration -> addOrUpdateStream(connection, jsm, streamNames, setupConfiguration));
            }
        } catch (Exception e) {
            // Either not allowed or stream already configured by another instance
            throw new JetStreamSetupException(String.format("Unable to configure stream: %s", e.getMessage()), e);
        }
    }

    public Optional<SetupResult> addOrUpdateStream(Connection connection, JetStreamSetupConfiguration setupConfiguration) {
        try {
            final var jsm = connection.jetStreamManagement();
            final var streamNames = jsm.getStreamNames();
            return addOrUpdateStream(connection, jsm, streamNames, setupConfiguration);
        } catch (IOException | JetStreamApiException e) {
            throw new JetStreamSetupException(String.format("Unable to configure stream: %s", e.getMessage()), e);
        }
    }

    private Optional<SetupResult> addOrUpdateStream(Connection connection, JetStreamManagement jsm, List<String> streamNames,
            JetStreamSetupConfiguration setupConfiguration) {
        try {
            if (streamNames.contains(setupConfiguration.stream())) {
                final var streamInfo = jsm.getStreamInfo(setupConfiguration.stream(), StreamInfoOptions.allSubjects());
                final var configuration = streamInfo.getConfiguration();
                final var currentSubjects = new HashSet<>(configuration.getSubjects());
                if (!currentSubjects.containsAll(setupConfiguration.subjects())) {
                    final var streamConfiguration = streamInfo.getConfiguration();
                    final var newSubjects = new HashSet<>(streamConfiguration.getSubjects());
                    newSubjects.addAll(setupConfiguration.subjects());
                    logger.debugf("Updating stream %s with subjects %s", streamConfiguration.getName(), newSubjects);
                    return Optional.of(new SetupResult(connection,
                            jsm.updateStream(StreamConfiguration.builder(streamConfiguration).subjects(newSubjects).build())));
                } else {
                    return Optional.of(new SetupResult(connection, streamInfo));
                }
            } else {
                logger.debugf("Creating stream: %s with subjects: %s", setupConfiguration.stream(),
                        setupConfiguration.subjects());
                StreamConfiguration.Builder streamConfigBuilder = StreamConfiguration.builder()
                        .name(setupConfiguration.stream())
                        .storageType(setupConfiguration.storageType())
                        .retentionPolicy(setupConfiguration.retentionPolicy())
                        .replicas(setupConfiguration.replicas())
                        .subjects(setupConfiguration.subjects());
                return Optional.of(new SetupResult(connection, jsm.addStream(streamConfigBuilder.build())));
            }
        } catch (JetStreamApiException e) {
            logger.warnf(e, "Unable to update stream: %s with message: %s", setupConfiguration.stream(), e.getMessage());
            return Optional.empty();
        } catch (IOException e) {
            throw new JetStreamSetupException(String.format("Unable to configure stream: %s", e.getMessage()), e);
        }
    }
}
