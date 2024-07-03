package io.quarkiverse.reactive.messaging.nats.jetstream.setup;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;

import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.KeyValueConfiguration;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import io.nats.client.api.StreamInfoOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamBuildConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;

public class JetStreamSetup {
    private static final Logger logger = Logger.getLogger(JetStreamSetup.class);

    public void setup(Connection connection,
            JetStreamBuildConfiguration jetStreamConfiguration) {
        try {
            if (jetStreamConfiguration.autoConfigure()) {
                JetStreamSetupConfiguration.of(jetStreamConfiguration)
                        .forEach(setupConfiguration -> addOrUpdateStream(connection, setupConfiguration));
                KeyValueSetupConfiguration.of(jetStreamConfiguration)
                        .forEach(
                                keyValueSetupConfiguration -> addOrUpdateKeyValueStore(connection, keyValueSetupConfiguration));
            }
        } catch (Exception e) {
            // Either not allowed or stream already configured by another instance
            throw new JetStreamSetupException(String.format("Unable to configure stream: %s", e.getMessage()), e);
        }
    }

    public Optional<SetupResult> addOrUpdateStream(Connection connection, JetStreamSetupConfiguration setupConfiguration) {
        try {
            final var jsm = connection.jetStreamManagement();
            return addOrUpdateStream(connection, jsm, setupConfiguration);
        } catch (IOException e) {
            throw new JetStreamSetupException(String.format("Unable to manage JetStream: %s", e.getMessage()), e);
        }
    }

    private Optional<SetupResult> addOrUpdateStream(Connection connection,
            JetStreamManagement jsm,
            JetStreamSetupConfiguration setupConfiguration) {
        return getStreamInfo(jsm, setupConfiguration.stream())
                .map(streamInfo -> updateStream(connection, jsm, streamInfo, setupConfiguration))
                .orElseGet(() -> createStream(connection, jsm, setupConfiguration));
    }

    private Optional<SetupResult> updateStream(Connection connection,
            JetStreamManagement jsm,
            StreamInfo streamInfo,
            JetStreamSetupConfiguration setupConfiguration) {
        try {
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
        } catch (IOException | JetStreamApiException e) {
            logger.warnf(e, "Unable to update stream: %s with message: %s", setupConfiguration.stream(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<SetupResult> createStream(Connection connection,
            JetStreamManagement jsm,
            JetStreamSetupConfiguration setupConfiguration) {
        try {
            StreamConfiguration.Builder streamConfigBuilder = StreamConfiguration.builder()
                    .name(setupConfiguration.stream())
                    .storageType(setupConfiguration.storageType())
                    .retentionPolicy(setupConfiguration.retentionPolicy())
                    .replicas(setupConfiguration.replicas())
                    .subjects(setupConfiguration.subjects());
            return Optional.of(new SetupResult(connection, jsm.addStream(streamConfigBuilder.build())));
        } catch (IOException | JetStreamApiException e) {
            logger.warnf(e, "Unable to create stream: %s with message: %s", setupConfiguration.stream(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<StreamInfo> getStreamInfo(JetStreamManagement jsm, String stream) {
        try {
            return Optional.of(jsm.getStreamInfo(stream, StreamInfoOptions.allSubjects()));
        } catch (IOException | JetStreamApiException e) {
            return Optional.empty();
        }
    }

    private void addOrUpdateKeyValueStore(Connection connection, KeyValueSetupConfiguration keyValueSetupConfiguration) {
        try {
            final var kvm = connection.keyValueManagement();
            if (kvm.getBucketNames().contains(keyValueSetupConfiguration.name())) {
                kvm.update(createKeyValueConfiguration(keyValueSetupConfiguration));
            } else {
                kvm.create(createKeyValueConfiguration(keyValueSetupConfiguration));
            }
        } catch (IOException | JetStreamApiException e) {
            throw new JetStreamSetupException(String.format("Unable to manage Key Value Store: %s", e.getMessage()), e);
        }
    }

    private KeyValueConfiguration createKeyValueConfiguration(KeyValueSetupConfiguration keyValueSetupConfiguration) {
        return KeyValueConfiguration.builder()
                .name(keyValueSetupConfiguration.name())
                .description(keyValueSetupConfiguration.description().orElse(null))
                .storageType(keyValueSetupConfiguration.storageType())
                .maxBucketSize(keyValueSetupConfiguration.maxBucketSize().orElse(null))
                .maxHistoryPerKey(keyValueSetupConfiguration.maxHistoryPerKey().orElse(null))
                .maxValueSize(keyValueSetupConfiguration.maxValueSize().orElse(null))
                .ttl(keyValueSetupConfiguration.ttl().orElse(null))
                .replicas(keyValueSetupConfiguration.replicas().orElse(null))
                .compression(keyValueSetupConfiguration.compressed())
                .build();
    }
}
