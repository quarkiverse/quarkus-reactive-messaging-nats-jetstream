package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import io.nats.client.StreamContext;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

public interface StreamContextAware {

    default Uni<StreamContext> streamContext(final Connection connection, final String stream) {
        return Uni.createFrom().item(Unchecked.supplier(() -> connection.getStreamContext(stream)));
    }

}
