package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import io.nats.client.KeyValue;
import io.nats.client.api.KeyValueStatus;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeyValueStore {

   public Uni<KeyValue> getKeyValue(Connection connection) {


   }
}
