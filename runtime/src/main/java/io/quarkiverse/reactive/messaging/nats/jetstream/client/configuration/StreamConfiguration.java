package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import io.nats.client.api.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConfiguration;

public interface StreamConfiguration {
    /**
     * Name of stream
     */
    String name();

    /**
     * Description of stream
     */
    Optional<String> description();

    /**
     * Stream subjects
     */
    Set<String> subjects();

    /**
     * The number of replicas a message must be stored. Default value is 1.
     */
    Integer replicas();

    /**
     * The storage type for stream data (File or Memory).
     */
    StorageType storageType();

    /**
     * Declares the retention policy for the stream. @see
     * <a href="https://docs.nats.io/jetstream/concepts/streams#retention-policies">Retention Policy</a>
     */
    RetentionPolicy retentionPolicy();

    /**
     * The compression option for this stream
     */
    CompressionOption compressionOption();

    /**
     * The maximum number of consumers for this stream
     */
    Optional<Long> maximumConsumers();

    /**
     * The maximum messages for this stream
     */
    Optional<Long> maximumMessages();

    /**
     * The maximum messages per subject for this stream
     */
    Optional<Long> maximumMessagesPerSubject();

    /**
     * The maximum number of bytes for this stream
     */
    Optional<Long> maximumBytes();

    /**
     * the maximum message age for this stream
     */
    Optional<Duration> maximumAge();

    /**
     * The maximum message size for this stream
     */
    Optional<Integer> maximumMessageSize();

    /**
     * The template json for this stream
     */
    Optional<String> templateOwner();

    /**
     * The discard policy for this stream
     */
    Optional<DiscardPolicy> discardPolicy();

    /**
     * The duplicate checking window stream configuration. Duration. ZERO means duplicate checking is not enabled
     */
    Optional<Duration> duplicateWindow();

    /**
     * The flag indicating if the stream allows rollup
     */
    Optional<Boolean> allowRollup();

    /**
     * The flag indicating if the stream allows direct message access
     */
    Optional<Boolean> allowDirect();

    /**
     * The flag indicating if the stream allows higher performance and unified direct access for mirrors as well
     */
    Optional<Boolean> mirrorDirect();

    /**
     * The flag indicating if deny delete is set for the stream
     */
    Optional<Boolean> denyDelete();

    /**
     * The flag indicating if deny purge is set for the stream
     */
    Optional<Boolean> denyPurge();

    /**
     * Whether discard policy with max message per subject is applied per subject
     */
    Optional<Boolean> discardNewPerSubject();

    /**
     * The first sequence used in the stream
     */
    Optional<Long> firstSequence();

    static StreamConfiguration of(JetStreamConfiguration.Stream stream) {
        return DefaultStreamConfiguration.builder()
                .name(stream.name())
                .description(stream.description().orElse(null))
                .subjects(stream.subjects())
                .replicas(stream.replicas())
                .storageType(stream.storageType())
                .retentionPolicy(stream.retentionPolicy())
                .compressionOption(stream.compressionOption())
                .maximumConsumers(stream.maximumConsumers().orElse(null))
                .maximumMessages(stream.maximumMessages().orElse(null))
                .maximumMessagesPerSubject(stream.maximumMessagesPerSubject().orElse(null))
                .maximumBytes(stream.maximumBytes().orElse(null))
                .maximumAge(stream.maximumAge().orElse(null))
                .maximumMessageSize(stream.maximumMessageSize().orElse(null))
                .templateOwner(stream.templateOwner().orElse(null))
                .discardPolicy(stream.discardPolicy().orElse(null))
                .duplicateWindow(stream.duplicateWindow().orElse(null))
                .allowRollup(stream.allowRollup().orElse(null))
                .allowDirect(stream.allowDirect().orElse(null))
                .mirrorDirect(stream.mirrorDirect().orElse(null))
                .denyDelete(stream.denyDelete().orElse(null))
                .denyPurge(stream.denyPurge().orElse(null))
                .discardNewPerSubject(stream.discardNewPerSubject().orElse(null))
                .firstSequence(stream.firstSequence().orElse(null))
                .build();
    }

    static StreamConfiguration of(io.nats.client.api.StreamConfiguration configuration) {
        return DefaultStreamConfiguration.builder()
                .name(configuration.getName())
                .description(configuration.getDescription())
                .subjects(new HashSet<>(configuration.getSubjects()))
                .replicas(configuration.getReplicas())
                .storageType(configuration.getStorageType())
                .retentionPolicy(configuration.getRetentionPolicy())
                .compressionOption(configuration.getCompressionOption())
                .maximumConsumers(configuration.getMaxConsumers())
                .maximumMessages((long) configuration.getMaximumMessageSize())
                .maximumMessagesPerSubject(configuration.getMaxMsgsPerSubject())
                .maximumBytes(configuration.getMaxBytes())
                .maximumAge(configuration.getMaxAge())
                .maximumMessageSize(configuration.getMaximumMessageSize())
                .templateOwner(configuration.getTemplateOwner())
                .discardPolicy(configuration.getDiscardPolicy())
                .duplicateWindow(configuration.getDuplicateWindow())
                .allowRollup(configuration.getAllowRollup())
                .allowDirect(configuration.getAllowDirect())
                .mirrorDirect(configuration.getMirrorDirect())
                .denyDelete(configuration.getDenyDelete())
                .denyPurge(configuration.getDenyPurge())
                .discardNewPerSubject(configuration.isDiscardNewPerSubject())
                .firstSequence(configuration.getFirstSequence())
                .build();
    }
}
