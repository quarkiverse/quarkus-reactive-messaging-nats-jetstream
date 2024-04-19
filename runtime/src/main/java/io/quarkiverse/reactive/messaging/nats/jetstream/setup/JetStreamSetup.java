package io.quarkiverse.reactive.messaging.nats.jetstream.setup;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamBuildConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionException;

public class JetStreamSetup {
    private static final Logger logger = Logger.getLogger(JetStreamSetup.class);

    public void setup(Connection connection,
            JetStreamBuildConfiguration jetStreamConfiguration) {
        try {
            if (jetStreamConfiguration.autoConfigure()) {
                final var jsm = connection.jetStreamManagement();
                getStreams(jetStreamConfiguration)
                        .stream()
                        .filter(streamConfig -> !streamConfig.subjects().isEmpty())
                        .forEach(streamConfig -> getStreamConfiguration(jsm, streamConfig)
                                .ifPresentOrElse(
                                        streamConfiguration -> updateStreamConfiguration(jsm, streamConfiguration, streamConfig,
                                                jetStreamConfiguration),
                                        () -> createStreamConfiguration(jsm, streamConfig, jetStreamConfiguration)));
            }
        } catch (Exception e) {
            // Either not allowed or stream already configured by another instance
            throw new JetStreamSetupException(String.format("Unable to configure stream: %s", e.getMessage()), e);
        }
    }

    public Set<String> addSubject(Connection connection, String stream, String subject) {
        try {
            final var jsm = connection.jetStreamManagement();
            final var streamInfo = jsm.getStreamInfo(stream, StreamInfoOptions.allSubjects());
            final var streamConfiguration = streamInfo.getConfiguration();
            if (!streamConfiguration.getSubjects().contains(subject)) {
                final var subjects = new HashSet<>(streamConfiguration.getSubjects());
                subjects.add(subject);
                logger.infof("Updating stream %s with subjects %s", streamConfiguration.getName(), subjects);
                jsm.updateStream(StreamConfiguration.builder(streamConfiguration).subjects(subjects).build());
                return subjects;
            } else {
                return new HashSet<>(streamConfiguration.getSubjects());
            }
        } catch (IOException | JetStreamApiException e) {
            throw new JetStreamSetupException(String.format("Unable to configure stream: %s", e.getMessage()), e);
        }
    }

    private Optional<StreamConfiguration> getStreamConfiguration(JetStreamManagement jsm, Stream stream) {
        return getStreamInfo(jsm, stream.name()).map(StreamInfo::getConfiguration);
    }

    private Optional<StreamInfo> getStreamInfo(JetStreamManagement jsm, String streamName) {
        try {
            return Optional.of(jsm.getStreamInfo(streamName, StreamInfoOptions.allSubjects()));
        } catch (IOException e) {
            throw new ConnectionException("Failed getting stream info: " + e.getMessage(), e);
        } catch (JetStreamApiException e) {
            return Optional.empty();
        }
    }

    private void createStreamConfiguration(JetStreamManagement jsm,
            Stream stream,
            JetStreamBuildConfiguration jetStreamConfiguration) {
        try {
            logger.infof("Creating stream: %s with subjects: %s", stream.name(), stream.subjects());
            StreamConfiguration.Builder streamConfigBuilder = StreamConfiguration.builder()
                    .name(stream.name())
                    .storageType(StorageType.valueOf(jetStreamConfiguration.storageType()))
                    .retentionPolicy(RetentionPolicy.valueOf(jetStreamConfiguration.retentionPolicy()))
                    .replicas(jetStreamConfiguration.replicas())
                    .subjects(stream.subjects());
            jsm.addStream(streamConfigBuilder.build());
        } catch (IOException | JetStreamApiException e) {
            throw new ConnectionException(
                    String.format("Failed creating stream: %s with message: %s", stream, e.getMessage()), e);
        }
    }

    private void updateStreamConfiguration(JetStreamManagement jsm,
            StreamConfiguration streamConfiguration,
            Stream stream,
            JetStreamBuildConfiguration jetStreamConfiguration) {
        try {
            if (!new HashSet<>(streamConfiguration.getSubjects()).containsAll(stream.subjects())) {
                logger.infof("Updating stream %s with subjects %s", streamConfiguration.getName(), stream.subjects());
                jsm.updateStream(StreamConfiguration.builder(streamConfiguration).subjects(stream.subjects())
                        .replicas(jetStreamConfiguration.replicas()).build());
            }
        } catch (IOException | JetStreamApiException e) {
            throw new ConnectionException(
                    String.format("Failed updating stream: %s with message: %s", stream, e.getMessage()), e);
        }
    }

    private List<Stream> getStreams(JetStreamBuildConfiguration configuration) {
        if (configuration.streams() != null) {
            return configuration.streams().stream().map(stream -> new Stream(stream.name(), stream.subjects()))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}
