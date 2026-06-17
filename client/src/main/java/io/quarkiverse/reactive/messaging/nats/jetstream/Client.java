package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.quarkiverse.reactive.messaging.nats.jetstream.stream.StreamInfo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface Client extends Publisher, Consumer, AutoCloseable {

    StreamManagement management();

    Uni<StreamInfo> stream(String stream);

    Multi<StreamInfo> streams();
}
