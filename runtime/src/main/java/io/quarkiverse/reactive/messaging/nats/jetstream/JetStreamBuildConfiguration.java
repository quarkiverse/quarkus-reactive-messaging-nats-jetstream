package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
     * The number of replicas a message must be stored. Default value is 1.
     */
    @WithDefault("1")
    Integer replicas();

    /**
     * The storage type for stream data (File or Memory).
     */
    @WithDefault("File")
    String storageType();

    /**
     * Declares the retention policy for the stream. @see
     * <a href="https://docs.nats.io/jetstream/concepts/streams#retention-policies">Retention Policy</a>
     */
    @WithDefault("Interest")
    String retentionPolicy();

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
        String storageType();

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
        Optional<Long> maxValueSize();

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
         * Name of stream
         */
        String name();

        /**
         * Stream subjects
         */
        Set<String> subjects();
    }
}
