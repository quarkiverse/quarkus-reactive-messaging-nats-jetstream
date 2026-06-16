package io.quarkiverse.reactive.messaging.nats.jetstream;

public interface Client extends Publisher, Consumer, AutoCloseable {

    StreamManagement management();

}
