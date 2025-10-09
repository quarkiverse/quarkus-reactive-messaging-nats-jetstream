package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import java.time.Duration;
import java.util.*;

import io.nats.client.api.CompressionOption;
import io.nats.client.api.DiscardPolicy;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;

public interface StreamConfiguration {

    /**
     * The name of the stream
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
     * The number of replicas. Default is 1.
     */
    Integer replicas();

    /**
     * The storage type for stream data (File or Memory). The default is File.
     */
    StorageType storageType();

    /**
     * Declares the retention policy for the stream. @see
     * <a href="https://docs.nats.io/jetstream/concepts/streams#retention-policies">Retention Policy</a>
     * Default is Interest.
     */
    RetentionPolicy retentionPolicy();

    /**
     * The compression option for this stream. The default is NONE.
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

}
