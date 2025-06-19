package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.nats.client.api.CompressionOption;
import io.nats.client.api.DiscardPolicy;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.quarkiverse.reactive.messaging.nats.jetstream.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.DefaultConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamSetupConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.StreamStatus;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.StreamConfiguration;
import io.quarkus.test.QuarkusUnitTest;

public class StreamSetupTest {

    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .withConfigurationResource("application-stream.properties");

    @Inject
    NatsConfiguration natsConfiguration;

    @Inject
    ConnectionFactory connectionFactory;

    @Test
    void updateConfiguration() throws Exception {
        try (final var connection = connectionFactory.create(ConnectionConfiguration.of(natsConfiguration),
                new DefaultConnectionListener()).await().atMost(Duration.ofSeconds(30))) {

            final var currentConfiguration = connection.streamManagement()
                    .onItem().transformToUni(streamManagement -> streamManagement.getStreamConfiguration("stream-test"))
                    .await().atMost(Duration.ofSeconds(30));
            assertThat(currentConfiguration).isNotNull();
            assertThat(currentConfiguration.subjects()).containsExactly("stream-data");
            assertThat(currentConfiguration.storageType()).isEqualTo(StorageType.File);
            assertThat(currentConfiguration.retentionPolicy()).isEqualTo(RetentionPolicy.Interest);

            final var notModifiedResult = connection.streamManagement()
                    .onItem().transformToUni(streamManagement -> streamManagement
                            .addStreams(List.of(StreamSetupConfiguration.builder()
                                    .configuration(update(Set.of("stream-data"), RetentionPolicy.Interest)).overwrite(false)
                                    .build())))
                    .await().atMost(Duration.ofSeconds(30));
            assertThat(notModifiedResult).hasSize(1);
            assertThat(notModifiedResult.get(0).status()).isEqualTo(StreamStatus.NotModified);

            final var notModifiedConfiguration = connection.streamManagement()
                    .onItem().transformToUni(streamManagement -> streamManagement.getStreamConfiguration("stream-test"))
                    .await().atMost(Duration.ofSeconds(30));
            assertThat(notModifiedConfiguration).isNotNull();
            assertThat(notModifiedConfiguration.subjects()).containsExactly("stream-data");
            assertThat(notModifiedConfiguration.storageType()).isEqualTo(StorageType.File);
            assertThat(notModifiedConfiguration.retentionPolicy()).isEqualTo(RetentionPolicy.Interest);

            final var updatedSubjectsResult = connection.streamManagement()
                    .onItem().transformToUni(streamManagement -> streamManagement
                            .addStreams(List.of(StreamSetupConfiguration.builder()
                                    .configuration(update(Set.of("stream-data", "stream-data-2"), RetentionPolicy.Interest))
                                    .overwrite(false).build())))
                    .await().atMost(Duration.ofSeconds(30));
            assertThat(updatedSubjectsResult).hasSize(1);
            assertThat(updatedSubjectsResult.get(0).status()).isEqualTo(StreamStatus.Updated);

            final var updatedSubjectsConfiguration = connection.streamManagement()
                    .onItem().transformToUni(streamManagement -> streamManagement.getStreamConfiguration("stream-test"))
                    .await().atMost(Duration.ofSeconds(30));
            assertThat(updatedSubjectsConfiguration).isNotNull();
            assertThat(updatedSubjectsConfiguration.subjects()).containsExactlyInAnyOrder("stream-data", "stream-data-2");

            final var updatedRetentionPolicyResult = connection.streamManagement()
                    .onItem()
                    .transformToUni(streamManagement -> streamManagement.addStreams(List.of(StreamSetupConfiguration.builder()
                            .configuration(update(Set.of("stream-data", "stream-data-2"), RetentionPolicy.WorkQueue))
                            .overwrite(true).build())))
                    .await().atMost(Duration.ofSeconds(30));
            assertThat(updatedRetentionPolicyResult).hasSize(1);
            assertThat(updatedRetentionPolicyResult.get(0).status()).isEqualTo(StreamStatus.Created);

            final var retentionPolicyConfiguration = connection.streamManagement()
                    .onItem().transformToUni(streamManagement -> streamManagement.getStreamConfiguration("stream-test"))
                    .await().atMost(Duration.ofSeconds(30));
            assertThat(retentionPolicyConfiguration).isNotNull();
            assertThat(retentionPolicyConfiguration.subjects()).containsExactlyInAnyOrder("stream-data", "stream-data-2");
            assertThat(retentionPolicyConfiguration.storageType()).isEqualTo(StorageType.File);
            assertThat(retentionPolicyConfiguration.retentionPolicy()).isEqualTo(RetentionPolicy.WorkQueue);
        }
    }

    private StreamConfiguration update(final Set<String> subjects, final RetentionPolicy retentionPolicy) {
        return new StreamConfiguration() {

            @Override
            public String name() {
                return "stream-test";
            }

            @Override
            public Optional<String> description() {
                return Optional.empty();
            }

            @Override
            public Set<String> subjects() {
                return subjects;
            }

            @Override
            public Integer replicas() {
                return 1;
            }

            @Override
            public StorageType storageType() {
                return StorageType.File;
            }

            @Override
            public RetentionPolicy retentionPolicy() {
                return retentionPolicy;
            }

            @Override
            public CompressionOption compressionOption() {
                return CompressionOption.None;
            }

            @Override
            public Optional<Long> maximumConsumers() {
                return Optional.empty();
            }

            @Override
            public Optional<Long> maximumMessages() {
                return Optional.empty();
            }

            @Override
            public Optional<Long> maximumMessagesPerSubject() {
                return Optional.empty();
            }

            @Override
            public Optional<Long> maximumBytes() {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> maximumAge() {
                return Optional.empty();
            }

            @Override
            public Optional<Integer> maximumMessageSize() {
                return Optional.empty();
            }

            @Override
            public Optional<String> templateOwner() {
                return Optional.empty();
            }

            @Override
            public Optional<DiscardPolicy> discardPolicy() {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> duplicateWindow() {
                return Optional.empty();
            }

            @Override
            public Optional<Boolean> allowRollup() {
                return Optional.empty();
            }

            @Override
            public Optional<Boolean> allowDirect() {
                return Optional.empty();
            }

            @Override
            public Optional<Boolean> mirrorDirect() {
                return Optional.empty();
            }

            @Override
            public Optional<Boolean> denyDelete() {
                return Optional.empty();
            }

            @Override
            public Optional<Boolean> denyPurge() {
                return Optional.empty();
            }

            @Override
            public Optional<Boolean> discardNewPerSubject() {
                return Optional.empty();
            }

            @Override
            public Optional<Long> firstSequence() {
                return Optional.empty();
            }
        };
    }
}
