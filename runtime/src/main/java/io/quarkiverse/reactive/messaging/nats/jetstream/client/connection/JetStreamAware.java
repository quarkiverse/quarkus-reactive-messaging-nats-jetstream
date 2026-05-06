package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import io.nats.client.JetStream;
import io.nats.client.JetStreamManagement;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

public interface JetStreamAware {

    default Uni<JetStream> jetStream(Connection connection) {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStream));
    }

    default Uni<JetStreamManagement> jetStreamManagement(Connection connection) {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStreamManagement));
    }
}
