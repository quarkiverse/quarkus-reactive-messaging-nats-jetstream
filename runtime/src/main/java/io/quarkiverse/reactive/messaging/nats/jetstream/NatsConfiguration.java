package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.*;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@ConfigMapping(prefix = "quarkus.messaging.nats")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface NatsConfiguration {

    /**
     * The connection configuration
     */
    ConnectionConfiguration connection();

    /**
     *  Automatically configure streams, consumers and key value stores.
     *  If a stream or a consumer already exists and the configuration does not match the existing configuration, then an exception is thrown.
     */
    @WithDefault("true")
    Boolean autoConfigure();

    /**
     * The JetStream configuration
     */
    Optional<JetStream> jetStream();

    /**
     * The configuration of key value stores. The key is the name of the bucket.
     */
    Map<String, KeyValueStoreConfiguration> keyValueStores();

    interface JetStream {

        /**
         * Enable tracing for JetStream
         */
        @WithDefault("true")
        Boolean trace();

        /**
         * The stream configurations. The map key is the name of the stream.
         */
        Map<String, Stream> streams();
    }

    interface Stream extends StreamConfiguration {

        /**
         * Outgoing channel configurations. The map key is the channel name.
         */
        Map<String, PublishConfiguration> outgoing();

        /**
         * Incoming channel configurations. The map key is the channel name.
         */
        Map<String, Consumer> incoming();


        interface Consumer {

            enum Type {
                Pull, Push
            }

            /**
             * The consumer configuration type to be used
             */
            @WithDefault("Pull")
            Type type();

            /**
             * The retry duration for retry publishing messages
             */
            @WithDefault("10s")
            Duration retryBackoff();

            /**
             *  The pull consumer configuration
             */
            <T> Optional<PullConsumerConfiguration<T>> pull();

            /**
             * The push consumer configuration
             */
            <T> Optional<PushConsumerConfiguration<T>> push();

        }
    }
}
