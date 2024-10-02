package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.util.HashSet;

import org.jboss.logging.Logger;

import io.nats.client.JetStreamManagement;
import io.nats.client.Nats;
import io.nats.client.api.KeyValueConfiguration;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import io.nats.client.api.StreamInfoOptions;
import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.SetupException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.SystemException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionOptionsFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueSetupConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.SetupConfiguration;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import io.smallrye.mutiny.Uni;

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
                        .forEach(
                                setupConfiguration -> addOrUpdateStream(connection, setupConfiguration).await().indefinitely());
                KeyValueSetupConfiguration.of(jetStreamConfiguration.getValue())
                        .forEach(keyValueSetupConfiguration -> addOrUpdateKeyValueStore(connection, keyValueSetupConfiguration)
                                .await().indefinitely());
            } catch (Throwable failure) {
                throw new SetupException(String.format("Unable to configure stream: %s", failure.getMessage()),
                        failure);
            }
        }
    }

    private io.nats.client.Connection connect() throws ConnectionException {
        try {
            final var connectionConfiguration = ConnectionConfiguration.of(natsConfiguration.getValue());
            final var optionsFactory = new ConnectionOptionsFactory();
            final var options = optionsFactory.create(connectionConfiguration);
            return Nats.connect(options);
        } catch (Throwable failure) {
            throw new ConnectionException(failure);
        }
    }

    private Uni<StreamInfo> addOrUpdateStream(io.nats.client.Connection connection, SetupConfiguration setupConfiguration) {
        return getJetStreamManagement(connection).onItem()
                .transformToUni(jetStreamManagement -> addOrUpdateStream(jetStreamManagement, setupConfiguration));
    }

    private Uni<Void> addOrUpdateKeyValueStore(io.nats.client.Connection connection,
            KeyValueSetupConfiguration keyValueSetupConfiguration) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                final var kvm = connection.keyValueManagement();
                if (kvm.getBucketNames().contains(keyValueSetupConfiguration.bucketName())) {
                    kvm.update(createKeyValueConfiguration(keyValueSetupConfiguration));
                } else {
                    kvm.create(createKeyValueConfiguration(keyValueSetupConfiguration));
                }
                emitter.complete(null);
            } catch (Throwable failure) {
                emitter.fail(new SetupException(String.format("Unable to manage Key Value Store: %s", failure.getMessage()),
                        failure));
            }
        });
    }

    private Uni<StreamInfo> addOrUpdateStream(JetStreamManagement jsm,
            SetupConfiguration setupConfiguration) {
        return getStreamInfo(jsm, setupConfiguration.stream())
                .onItem().transformToUni(streamInfo -> updateStream(jsm, streamInfo, setupConfiguration))
                .onFailure().recoverWithUni(failure -> createStream(jsm, setupConfiguration));
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

    private Uni<StreamInfo> createStream(JetStreamManagement jsm,
            SetupConfiguration setupConfiguration) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                StreamConfiguration.Builder streamConfigBuilder = StreamConfiguration.builder()
                        .name(setupConfiguration.stream())
                        .storageType(setupConfiguration.storageType())
                        .retentionPolicy(setupConfiguration.retentionPolicy())
                        .replicas(setupConfiguration.replicas())
                        .subjects(setupConfiguration.subjects());
                emitter.complete(jsm.addStream(streamConfigBuilder.build()));
            } catch (Throwable failure) {
                emitter.fail(new SetupException(String.format("Unable to create stream: %s with message: %s",
                        setupConfiguration.stream(), failure.getMessage()), failure));
            }
        });
    }

    private Uni<StreamInfo> updateStream(JetStreamManagement jsm,
            StreamInfo streamInfo,
            SetupConfiguration setupConfiguration) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                final var configuration = streamInfo.getConfiguration();
                final var currentSubjects = new HashSet<>(configuration.getSubjects());
                if (!currentSubjects.containsAll(setupConfiguration.subjects())) {
                    final var streamConfiguration = streamInfo.getConfiguration();
                    final var newSubjects = new HashSet<>(streamConfiguration.getSubjects());
                    newSubjects.addAll(setupConfiguration.subjects());
                    logger.debugf("Updating stream %s with subjects %s", streamConfiguration.getName(), newSubjects);
                    emitter.complete(
                            jsm.updateStream(StreamConfiguration.builder(streamConfiguration).subjects(newSubjects).build()));
                } else {
                    emitter.complete(streamInfo);
                }
            } catch (Throwable failure) {
                emitter.fail(new SetupException(String.format("Unable to update stream: %s with message: %s",
                        setupConfiguration.stream(), failure.getMessage()), failure));
            }
        });
    }

    private Uni<JetStreamManagement> getJetStreamManagement(io.nats.client.Connection connection) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                emitter.complete(connection.jetStreamManagement());
            } catch (Throwable failure) {
                emitter.fail(
                        new SystemException(String.format("Unable to manage JetStream: %s", failure.getMessage()), failure));
            }
        });
    }

    private Uni<StreamInfo> getStreamInfo(JetStreamManagement jsm, String streamName) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                emitter.complete(jsm.getStreamInfo(streamName, StreamInfoOptions.allSubjects()));
            } catch (Throwable failure) {
                emitter.fail(new SystemException(
                        String.format("Unable to read stream %s with message: %s", streamName, failure.getMessage()), failure));
            }
        });
    }
}
