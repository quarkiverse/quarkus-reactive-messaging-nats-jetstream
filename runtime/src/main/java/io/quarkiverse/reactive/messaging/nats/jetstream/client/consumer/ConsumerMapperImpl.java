package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import io.nats.client.api.ClusterInfo;
import io.nats.client.api.ConsumerInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Cluster;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Replica;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ConsumerMapperImpl implements ConsumerMapper {

    @Override
    public Consumer map(ConsumerInfo consumerInfo) {
        return Consumer.builder()
                .pending(consumerInfo.getNumPending())
                .waiting(consumerInfo.getNumWaiting())
                .acknowledgePending(consumerInfo.getNumAckPending())
                .cluster(map(consumerInfo.getClusterInfo()))
                .configuration(map(consumerInfo.getConsumerConfiguration()))
                .stream(consumerInfo.getStreamName())
                .created(consumerInfo.getCreationTime())
                .build();
    }

    private Cluster map(ClusterInfo clusterInfo) {
        if (clusterInfo != null) {
            return Cluster.builder()
                    .name(clusterInfo.getName())
                    .leader(clusterInfo.getLeader())
                    .replicas(map(clusterInfo.getReplicas()))
                    .build();
        }
        return null;
    }

    private List<Replica> map(List<io.nats.client.api.Replica> replicas) {
        if (replicas != null) {
            return replicas.stream().map(this::map).toList();
        } else {
            return List.of();
        }
    }

    private Replica map(io.nats.client.api.Replica replica) {
        return Replica.builder()
                .name(replica.getName())
                .lag(replica.getLag())
                .active(replica.getActive())
                .current(replica.isCurrent())
                .offline(replica.isOffline())
                .build();
    }

    private ConsumerConfiguration map(io.nats.client.api.ConsumerConfiguration consumerConfiguration) {
        return ConsumerConfiguration.builder()
                .ackPolicy(consumerConfiguration.getAckPolicy())
                .deliverPolicy(consumerConfiguration.getDeliverPolicy())
                .durable(consumerConfiguration.getDurable())
                .ackWait(consumerConfiguration.getAckWait())
                .backoff(consumerConfiguration.getBackoff())
                .deliverGroup(consumerConfiguration.getDeliverGroup())
                .deliverSubject(consumerConfiguration.getDeliverSubject())
                .description(consumerConfiguration.getDescription())
                .filterSubjects(consumerConfiguration.getFilterSubjects())
                .flowControl(consumerConfiguration.isFlowControl())
                .headersOnly(consumerConfiguration.isHeadersOnly())
                .idleHeartbeat(consumerConfiguration.getIdleHeartbeat())
                .inactiveThreshold(consumerConfiguration.getInactiveThreshold())
                .maxAckPending(consumerConfiguration.getMaxAckPending())
                .maxDeliver(consumerConfiguration.getMaxDeliver())
                .maxBatch(consumerConfiguration.getMaxBatch())
                .maxExpires(consumerConfiguration.getMaxExpires())
                .maxBytes(consumerConfiguration.getMaxBytes())
                .maxPullWaiting(consumerConfiguration.getMaxPullWaiting())
                .memStorage(consumerConfiguration.isMemStorage())
                .numReplicas(consumerConfiguration.getNumReplicas())
                .metadata(consumerConfiguration.getMetadata())
                .name(consumerConfiguration.getName())
                .pauseUntil(consumerConfiguration.getPauseUntil())
                .replayPolicy(consumerConfiguration.getReplayPolicy())
                .sampleFrequency(consumerConfiguration.getSampleFrequency())
                .rateLimit(consumerConfiguration.getRateLimit())
                .startSequence(consumerConfiguration.getStartSequence())
                .startTime(consumerConfiguration.getStartTime())
                .build();
    }
}
