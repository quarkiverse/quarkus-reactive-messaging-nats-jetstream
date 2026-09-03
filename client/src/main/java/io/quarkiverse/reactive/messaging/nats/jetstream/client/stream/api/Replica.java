package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.time.Duration;

import org.jspecify.annotations.NonNull;

public interface Replica {

    /**
     * The server name of the peer
     *
     * @return the name
     */
    @NonNull
    String name();

    /**
     * Indicates if the server is up-to-date and synchronised
     *
     * @return if is current
     */
    boolean current();

    /**
     * Indicates the node is considered offline by the group
     *
     * @return if is offline
     */
    boolean offline();

    /**
     * Time since this peer was last seen
     *
     * @return the active time
     */
    @NonNull
    Duration active();

    /**
     * How many uncommitted operations this peer is behind the leader
     *
     * @return the lag
     */
    long lag();
}
