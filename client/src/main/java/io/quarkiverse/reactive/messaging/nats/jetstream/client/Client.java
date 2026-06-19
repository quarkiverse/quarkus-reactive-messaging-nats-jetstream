package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamInfo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;

public interface Client extends Publisher, Consumer, KeyValueStore, ObjectStore, AutoCloseable {

    @NonNull StreamManagement management();

    @NonNull Uni<StreamInfo> stream(@NonNull String stream);

    @NonNull Multi<StreamInfo> streams();

}
