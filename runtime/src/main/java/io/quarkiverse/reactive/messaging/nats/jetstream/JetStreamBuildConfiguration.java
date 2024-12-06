package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.nats.client.api.*;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.messaging.nats.jet-stream")
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public interface JetStreamBuildConfiguration {

    /**
     * Autoconfigure stream and subjects based on channel configuration
     */
    @WithDefault("true")
    Boolean autoConfigure();

    /**
     * Enable tracing for JetStream
     */
    @WithDefault("true")
    Boolean trace();

    /**
     * If auto-configure is true the streams are created on Nats server.
     */
    List<Stream> streams();

    /**
     * If auto-configure is true the key-value stores are created on Nats server.
     */
    List<KeyValueStore> keyValueStores();

    interface KeyValueStore {
        /**
         * Name of Key-Value store
         */
        String bucketName();

        /**
         * Description of Key-Value store
         */
        Optional<String> description();

        /**
         * The storage type (File or Memory).
         */
        @WithDefault("File")
        StorageType storageType();

        /**
         * The maximum number of bytes for this bucket
         */
        Optional<Long> maxBucketSize();

        /**
         * The maximum number of history for any one key. Includes the current value.
         */
        Optional<Integer> maxHistoryPerKey();

        /**
         * The maximum size for an individual value in the bucket.
         */
        Optional<Integer> maxValueSize();

        /**
         * The maximum age for a value in this bucket
         */
        Optional<String> ttl();

        /**
         * The number of replicas for this bucket
         */
        Optional<Integer> replicas();

        /**
         * Sets whether to use compression
         */
        @WithDefault("true")
        Boolean compressed();
    }

    interface Stream {

        /**
         * If the stream already exists, it will be overwritten
         */
        @WithDefault("false")
        Boolean overwrite();

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
        @WithDefault("1")
        Integer replicas();

        /**
         * The storage type for stream data (File or Memory).
         */
        @WithDefault("File")
        StorageType storageType();

        /**
         * Declares the retention policy for the stream. @see
         * <a href="https://docs.nats.io/jetstream/concepts/streams#retention-policies">Retention Policy</a>
         */
        @WithDefault("Interest")
        RetentionPolicy retentionPolicy();

        /**
         * The compression option for this stream
         */
        @WithDefault("none")
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
    }
}
