package io.quarkiverse.reactive.messsaging.nats.jetstream;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.reactive-messaging.nats.jet-stream")
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
}
