package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.util.List;
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
     *
     * The setup process also reads the application configuration to setup configured streams from channel configuration.
     */
    List<Stream> streams();

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
