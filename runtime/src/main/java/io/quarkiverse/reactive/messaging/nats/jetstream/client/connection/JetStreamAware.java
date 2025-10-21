package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import io.nats.client.JetStream;
import io.nats.client.JetStreamManagement;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

@SuppressWarnings("resource")
public interface JetStreamAware {

    Connection connection();

    default Uni<JetStream> jetStream() {
        return Uni.createFrom().item(Unchecked.supplier(() -> connection().jetStream()));
    }

    default Uni<JetStreamManagement> jetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(() -> connection().jetStreamManagement()));
    }
}
