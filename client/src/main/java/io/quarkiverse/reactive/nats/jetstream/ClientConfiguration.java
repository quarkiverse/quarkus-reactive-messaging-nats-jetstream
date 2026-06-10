package io.quarkiverse.reactive.nats.jetstream;

import io.quarkiverse.reactive.nats.jetstream.connection.ConnectionConfiguration;
import io.quarkiverse.reactive.nats.jetstream.message.MessageConfiguration;
import org.jspecify.annotations.NonNull;

public interface ClientConfiguration {

    @NonNull
    ConnectionConfiguration connection();

    @NonNull
    MessageConfiguration message();

}
