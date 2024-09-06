package io.quarkiverse.reactive.messaging.nats.jetstream.client.vertx;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.jboss.logging.Logger;

import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.AdministrationException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.administration.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.administration.StreamState;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueSetupConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.SetupConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.delegates.ConnectionDelegate;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

public class AdministrationConnection
        implements io.quarkiverse.reactive.messaging.nats.jetstream.client.AdministrationConnection {
    private final static Logger logger = Logger.getLogger(AdministrationConnection.class);

    private final io.nats.client.Connection connection;
    private final List<ConnectionListener> listeners;
    private final ConnectionDelegate connectionDelegate;

    public AdministrationConnection(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener) {
        this.connectionDelegate = new ConnectionDelegate();
        this.connection = connectionDelegate.connect(this, connectionConfiguration);
        this.listeners = new ArrayList<>(List.of(connectionListener));
    }

    @Override
    public Uni<ConsumerInfo> getConsumerInfo(String stream, String consumerName) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var jsm = connection.jetStreamManagement();
                return jsm.getConsumerInfo(stream, consumerName);
            } catch (IOException | JetStreamApiException e) {
                throw new AdministrationException(e);
            }
        }));
    }

    @Override
    public Uni<List<String>> getStreams() {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var jsm = connection.jetStreamManagement();
                return jsm.getStreamNames();
            } catch (IOException | JetStreamApiException e) {
                throw new AdministrationException(e);
            }
        }));
    }

    @Override
    public Uni<List<String>> getSubjects(String streamName) {
        return Uni.createFrom()
                .item(Unchecked.supplier(() -> getStreamInfo(streamName).map(streamInfo -> streamInfo.getConfiguration()
                        .getSubjects()).orElseGet(List::of)));
    }

    @Override
    public Uni<List<String>> getConsumerNames(String streamName) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var jsm = connection.jetStreamManagement();
                return jsm.getConsumerNames(streamName);
            } catch (IOException | JetStreamApiException e) {
                throw new AdministrationException(e);
            }
        }));
    }

    @Override
    public Uni<PurgeResult> purgeStream(String streamName) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var jsm = connection.jetStreamManagement();
                final var response = jsm.purgeStream(streamName);
                return new PurgeResult(streamName, response.isSuccess(), response.getPurged());
            } catch (IOException | JetStreamApiException e) {
                throw new AdministrationException(e);
            }
        }));
    }

    @Override
    public Uni<Void> deleteMessage(String stream, long sequence, boolean erase) {
        return Uni.createFrom().<Void> item(Unchecked.supplier(() -> {
            try {
                final var jsm = connection.jetStreamManagement();
                if (!jsm.deleteMessage(stream, sequence, erase)) {
                    throw new DeleteException(
                            String.format("Unable to delete message in stream %s with sequence %d", stream, sequence));
                }
                return null;
            } catch (IOException | JetStreamApiException e) {
                throw new DeleteException(String.format("Unable to delete message in stream %s with sequence %d: %s", stream,
                        sequence, e.getMessage()), e);
            }
        }));
    }

    @Override
    public Uni<StreamState> getStreamState(String streamName) {
        return Uni.createFrom()
                .item(Unchecked
                        .supplier(() -> getStreamInfo(streamName).map(streamInfo -> StreamState.of(streamInfo.getStreamState()))
                                .orElseThrow(() -> new AdministrationException("Stream state not found"))));
    }

    @Override
    public Uni<List<PurgeResult>> purgeAllStreams() {
        return getStreams()
                .onItem().transformToUni(this::purgeAllStreams);
    }

    @Override
    public boolean isConnected() {
        return connectionDelegate.isConnected(this::connection);
    }

    @Override
    public Uni<Void> flush(Duration duration) {
        return connectionDelegate.flush(this::connection, duration);
    }

    @Override
    public List<ConnectionListener> listeners() {
        return listeners;
    }

    @Override
    public void addListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void close() throws Exception {
        connectionDelegate.close(this::connection);
    }

    @Override
    public Uni<SetupResult> addOrUpdateStream(SetupConfiguration setupConfiguration) {
        return getJetStreamManagement().onItem()
                .transformToUni(jetStreamManagement -> addOrUpdateStream(jetStreamManagement, setupConfiguration));
    }

    @Override
    public Uni<Void> addOrUpdateKeyValueStore(KeyValueSetupConfiguration keyValueSetupConfiguration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var kvm = connection.keyValueManagement();
                if (kvm.getBucketNames().contains(keyValueSetupConfiguration.bucketName())) {
                    kvm.update(createKeyValueConfiguration(keyValueSetupConfiguration));
                } else {
                    kvm.create(createKeyValueConfiguration(keyValueSetupConfiguration));
                }
                return null;
            } catch (IOException | JetStreamApiException e) {
                throw new JetStreamSetupException(String.format("Unable to manage Key Value Store: %s", e.getMessage()), e);
            }
        }));
    }

    private Uni<JetStreamManagement> getJetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                return connection.jetStreamManagement();
            } catch (IOException e) {
                throw new AdministrationException(String.format("Unable to manage JetStream: %s", e.getMessage()), e);
            }
        }));
    }

    private Uni<SetupResult> addOrUpdateStream(JetStreamManagement jsm,
            SetupConfiguration setupConfiguration) {
        return getStreamInfo(jsm, setupConfiguration.stream())
                .onItem().transformToUni(streamInfo -> updateStream(jsm, streamInfo, setupConfiguration))
                .onFailure().invoke(failure -> logger.warn(failure.getMessage(), failure))
                .onFailure().recoverWithUni(failure -> createStream(jsm, setupConfiguration));
    }

    private Connection connection() {
        return connection;
    }

    private Optional<StreamInfo> getStreamInfo(String streamName) {
        try {
            final var jsm = connection.jetStreamManagement();
            return Optional.of(jsm.getStreamInfo(streamName, StreamInfoOptions.allSubjects()));
        } catch (IOException | JetStreamApiException e) {
            logger.debugf(e, "Unable to read stream %s with message: %s", streamName, e.getMessage());
            return Optional.empty();
        }
    }

    private Uni<StreamInfo> getStreamInfo(JetStreamManagement jsm, String streamName) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                return jsm.getStreamInfo(streamName, StreamInfoOptions.allSubjects());
            } catch (IOException | JetStreamApiException e) {
                throw new AdministrationException(
                        String.format("Unable to read stream %s with message: %s", streamName, e.getMessage()), e);
            }
        }));
    }

    private Optional<PurgeResult> purgeStream(JetStreamManagement jetStreamManagement, String streamName) {
        try {
            final var response = jetStreamManagement.purgeStream(streamName);
            return Optional.of(new PurgeResult(streamName, response.isSuccess(), response.getPurged()));
        } catch (IOException | JetStreamApiException e) {
            logger.warnf(e, "Unable to purge stream %s with message: %s", streamName, e.getMessage());
            return Optional.empty();
        }
    }

    private Uni<List<PurgeResult>> purgeAllStreams(List<String> streams) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var jsm = connection.jetStreamManagement();
                return streams.stream().flatMap(streamName -> purgeStream(jsm, streamName).stream()).toList();
            } catch (IOException e) {
                throw new AdministrationException(e);
            }
        }));
    }

    private Uni<SetupResult> updateStream(JetStreamManagement jsm,
            StreamInfo streamInfo,
            SetupConfiguration setupConfiguration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var configuration = streamInfo.getConfiguration();
                final var currentSubjects = new HashSet<>(configuration.getSubjects());
                if (!currentSubjects.containsAll(setupConfiguration.subjects())) {
                    final var streamConfiguration = streamInfo.getConfiguration();
                    final var newSubjects = new HashSet<>(streamConfiguration.getSubjects());
                    newSubjects.addAll(setupConfiguration.subjects());
                    logger.debugf("Updating stream %s with subjects %s", streamConfiguration.getName(), newSubjects);
                    return new SetupResult(
                            jsm.updateStream(StreamConfiguration.builder(streamConfiguration).subjects(newSubjects).build()));
                } else {
                    return new SetupResult(streamInfo);
                }
            } catch (IOException | JetStreamApiException e) {
                throw new JetStreamSetupException(String.format("Unable to update stream: %s with message: %s",
                        setupConfiguration.stream(), e.getMessage()), e);
            }
        }));
    }

    private Uni<SetupResult> createStream(JetStreamManagement jsm,
            SetupConfiguration setupConfiguration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                StreamConfiguration.Builder streamConfigBuilder = StreamConfiguration.builder()
                        .name(setupConfiguration.stream())
                        .storageType(setupConfiguration.storageType())
                        .retentionPolicy(setupConfiguration.retentionPolicy())
                        .replicas(setupConfiguration.replicas())
                        .subjects(setupConfiguration.subjects());
                return new SetupResult(jsm.addStream(streamConfigBuilder.build()));
            } catch (IOException | JetStreamApiException e) {
                throw new JetStreamSetupException(String.format("Unable to create stream: %s with message: %s",
                        setupConfiguration.stream(), e.getMessage()), e);
            }
        }));
    }

    private KeyValueConfiguration createKeyValueConfiguration(KeyValueSetupConfiguration keyValueSetupConfiguration) {
        var builder = KeyValueConfiguration.builder();
        builder = builder.name(keyValueSetupConfiguration.bucketName());
        builder = keyValueSetupConfiguration.description().map(builder::description).orElse(builder);
        builder = builder.storageType(keyValueSetupConfiguration.storageType());
        builder = keyValueSetupConfiguration.maxBucketSize().map(builder::maxBucketSize).orElse(builder);
        builder = keyValueSetupConfiguration.maxHistoryPerKey().map(builder::maxHistoryPerKey).orElse(builder);
        builder = keyValueSetupConfiguration.maxValueSize().map(builder::maximumValueSize).orElse(builder);
        builder = keyValueSetupConfiguration.ttl().map(builder::ttl).orElse(builder);
        builder = keyValueSetupConfiguration.replicas().map(builder::replicas).orElse(builder);
        builder = builder.compression(keyValueSetupConfiguration.compressed());
        return builder.build();
    }
}
