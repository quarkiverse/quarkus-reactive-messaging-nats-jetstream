package io.quarkiverse.reactive.nats.consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * @see io.nats.client.api.ConsumerInfo
 */
public interface ConsumerInfo {

    @NonNull ConsumerConfiguration getConsumerConfiguration();

    @NonNull String getName();

    @NonNull String getStreamName();

    @NonNull ZonedDateTime getCreationTime();

    @NonNull SequenceInfo getDelivered();

    @NonNull SequenceInfo getAckFloor();

    long getNumPending();

    long getNumWaiting();

    long getNumAckPending();

    long getRedelivered();

    boolean getPaused();

    @Nullable Duration getPauseRemaining();

    @Nullable ClusterInfo getClusterInfo();

    boolean isPushBound();

    @Nullable ZonedDateTime getTimestamp();

    @Nullable List<PriorityGroupState> getPriorityGroupStates();

    long getCalculatedPending();

}
