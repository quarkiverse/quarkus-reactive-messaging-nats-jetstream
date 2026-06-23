package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionConfiguration;
import io.smallrye.mutiny.Uni;

public interface ClientFactory {

    @NonNull
    Uni<Client> create(@NonNull ConnectionConfiguration configuration, @NonNull ExecutorService executorService);

}
