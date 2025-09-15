package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;

public interface ConnectionFactory {

    @NonNull Uni<Connection> create();

}
