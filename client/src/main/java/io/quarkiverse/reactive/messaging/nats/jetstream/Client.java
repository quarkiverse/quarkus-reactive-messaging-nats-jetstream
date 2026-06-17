package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.quarkiverse.reactive.messaging.nats.jetstream.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.stream.StreamInfo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;
import org.jspecify.annotations.NonNull;

public interface Client extends Publisher, Consumer, AutoCloseable {

    @NonNull StreamManagement management();

    @NonNull Uni<StreamInfo> stream(@NonNull String stream);

    @NonNull Multi<StreamInfo> streams();

    @NonNull ClientConfiguration configuration();

    @NonNull Connection connection();

    @NonNull Context context();
}
