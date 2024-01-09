package io.quarkiverse.reactive.messsaging.nats.jetstream.setup;

import static io.quarkiverse.reactive.messsaging.nats.jetstream.JetStreamConnector.CONNECTOR_NAME;

import java.io.IOException;
import java.util.*;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.*;
import io.quarkiverse.reactive.messsaging.nats.jetstream.JetStreamBuildConfiguration;
import io.quarkiverse.reactive.messsaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messsaging.nats.jetstream.client.ConnectionException;

public class JetStreamSetup {
    private static final Logger logger = Logger.getLogger(JetStreamSetup.class);

    public void setup(Connection connection,
            JetStreamBuildConfiguration jetStreamConfiguration) {
        try {
            final var jsm = connection.jetStreamManagement();
            getStreams()
                    .stream()
                    .filter(streamConfig -> !streamConfig.subjects().isEmpty())
                    .forEach(streamConfig -> getStreamConfiguration(jsm, streamConfig)
                            .ifPresentOrElse(
                                    streamConfiguration -> updateStreamConfiguration(jsm, streamConfiguration, streamConfig,
                                            jetStreamConfiguration),
                                    () -> createStreamConfiguration(jsm, streamConfig, jetStreamConfiguration)));
        } catch (Exception e) {
            // Either not allowed or stream already configured by another instance
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

    private Collection<Stream> getStreams() {
        final var configs = new HashMap<String, Stream>();
        final var config = ConfigProvider.getConfig();
        getChannelPrefixes(config).forEach(channelPrefix -> {
            if (isNatsConnector(config, channelPrefix) && (autoConfigure(config, channelPrefix))) {
                getStream(config, channelPrefix).ifPresent(streamName -> {
                    final var streamConfig = Optional.ofNullable(configs.get(streamName))
                            .orElseGet(() -> new Stream(streamName, new HashSet<>()));
                    getSubject(config, channelPrefix).ifPresent(subject -> streamConfig.subjects().add(subject));
                    configs.putIfAbsent(streamName, streamConfig);
                });

            }
        });
        return configs.values();
    }

    private Set<String> getChannelPrefixes(Config config) {
        final var channelPrefixes = new HashSet<String>();
        config.getPropertyNames().forEach(propertyName -> {
            if (propertyName.startsWith("mp.messaging.incoming.")) {
                final var index = propertyName.indexOf(".", "mp.messaging.incoming.".length());
                channelPrefixes.add(propertyName.substring(0, index));
            } else if (propertyName.startsWith("mp.messaging.outgoing.")) {
                var index = propertyName.indexOf(".", "mp.messaging.outgoing.".length());
                channelPrefixes.add(propertyName.substring(0, index));
            }
        });
        return channelPrefixes;
    }

    private boolean isNatsConnector(Config config, String channelPrefix) {
        return config.getOptionalValue(channelPrefix + ".connector", String.class).filter(CONNECTOR_NAME::equals).isPresent();
    }

    private Optional<String> getStream(Config config, String channelPrefix) {
        return config.getOptionalValue(channelPrefix + ".stream", String.class);
    }

    private Optional<String> getSubject(Config config, String channelPrefix) {
        return config.getOptionalValue(channelPrefix + ".subject", String.class).map(subject -> {
            if (subject.endsWith(".>")) {
                return subject.substring(0, subject.length() - 2);
            } else {
                return subject;
            }
        });
    }

    private boolean autoConfigure(Config config, String channelPrefix) {
        return config.getOptionalValue(channelPrefix + ".auto-configure", Boolean.class).orElse(true);
    }
}
