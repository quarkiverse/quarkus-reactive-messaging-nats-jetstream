package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.util.*;

import io.nats.client.api.CompressionOption;
import io.nats.client.api.DiscardPolicy;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.smallrye.config.WithDefault;

public interface StreamConfiguration {

    /**
     * Description of stream
     */
    Optional<String> description();

    /**
     * Stream subjects
     */
    Optional<Set<String>> subjects();

    /**
     * The number of replicas. Default is 1.
     */
    @WithDefault("1")
    Integer replicas();

    /**
     * The storage type for stream data (File or Memory). The default is File.
     */
    @WithDefault("File")
    StorageType storageType();

    /**
     * Declares the retention policy for the stream. @see
     * <a href="https://docs.nats.io/jetstream/concepts/streams#retention-policies">Retention Policy</a>
     * Default is Interest.
     */
    @WithDefault("Interest")
    RetentionPolicy retentionPolicy();

    /**
     * The compression option for this stream. The default is NONE.
     */
    @WithDefault("NONE")
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

    /**
     * Pull consumer configurations. The map key is the name of the consumer.
     */
    Map<String, PullConsumerConfiguration> pullConsumers();

    /**
     * Push consumer configurations. The map key is the name of the consumer.
     */
    Map<String, PushConsumerConfiguration> pushConsumers();

    default Set<String> allSubjects() {
        final var subjects = new HashSet<String>();
        subjects().ifPresent(streamSubjects -> streamSubjects.forEach(subject -> subjects.add(escape(subject))));
        pullConsumers().values().stream()
                .map(PullConsumerConfiguration::consumerConfiguration)
                .forEach(consumer -> subjects.addAll(consumer.filterSubjects().stream().map(this::escape).toList()));
        pushConsumers().values().stream()
                .map(PushConsumerConfiguration::consumerConfiguration)
                .forEach(consumer -> subjects.addAll(consumer.filterSubjects().stream().map(this::escape).toList()));
        return subjects;
    }

    private String escape(String subject) {
        if (subject.endsWith(".>")) {
            return subject.substring(0, subject.length() - 2);
        } else {
            return subject;
        }
    }

    static StreamConfiguration of(io.nats.client.api.StreamConfiguration configuration) {
        return DefaultStreamConfiguration.builder()
                .description(Optional.ofNullable(configuration.getDescription()))
                .subjects(Optional.of(new HashSet<>(configuration.getSubjects())))
                .replicas(configuration.getReplicas())
                .storageType(configuration.getStorageType())
                .retentionPolicy(configuration.getRetentionPolicy())
                .compressionOption(configuration.getCompressionOption())
                .maximumConsumers(Optional.of(configuration.getMaxConsumers()))
                .maximumMessages(Optional.of((long) configuration.getMaximumMessageSize()))
                .maximumMessagesPerSubject(Optional.of(configuration.getMaxMsgsPerSubject()))
                .maximumBytes(Optional.of(configuration.getMaxBytes()))
                .maximumAge(Optional.of(configuration.getMaxAge()))
                .maximumMessageSize(Optional.of(configuration.getMaximumMessageSize()))
                .templateOwner(Optional.ofNullable(configuration.getTemplateOwner()))
                .discardPolicy(Optional.ofNullable(configuration.getDiscardPolicy()))
                .duplicateWindow(Optional.ofNullable(configuration.getDuplicateWindow()))
                .allowRollup(Optional.of(configuration.getAllowRollup()))
                .allowDirect(Optional.of(configuration.getAllowDirect()))
                .mirrorDirect(Optional.of(configuration.getMirrorDirect()))
                .denyDelete(Optional.of(configuration.getDenyDelete()))
                .discardNewPerSubject(Optional.of(configuration.isDiscardNewPerSubject()))
                .firstSequence(Optional.of(configuration.getFirstSequence()))
                .build();
    }

}
