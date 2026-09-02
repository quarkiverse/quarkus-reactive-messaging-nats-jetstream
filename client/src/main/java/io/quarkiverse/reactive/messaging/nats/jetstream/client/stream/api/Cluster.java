package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

public interface Cluster {

    /**
     * The cluster name. Technically can be null
     *
     * @return the cluster or null
     */
    @NonNull
    Optional<String> name();

    /**
     * In clustered environments the name of the Raft group managing the asset
     *
     * @return the raft group name or null
     */
    @NonNull
    Optional<String> raftGroup();

    /**
     * The server name of the RAFT leader
     *
     * @return the leader or null
     */
    @NonNull
    Optional<String> leader();

    /**
     * The time that it was elected as leader, absent when not the leader
     *
     * @return the time or null
     */
    @NonNull
    Optional<ZonedDateTime> leaderSince();

    /**
     * Indicates if the traffic_account is the system account. When true, replication traffic goes over the system account.
     *
     * @return true if the traffic_account is the system account
     */
    boolean systemAccount();

    /**
     * The account where the replication traffic goes over.
     *
     * @return the traffic account or null
     */
    @NonNull
    Optional<String> trafficAccount();

    /**
     * The members of the RAFT cluster. May be null if there are no replicas.
     *
     * @return the replicas or null
     */
    @NonNull
    List<Replica> replicas();

}
