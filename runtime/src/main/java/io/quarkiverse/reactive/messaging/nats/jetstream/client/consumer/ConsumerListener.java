package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import org.jspecify.annotations.NonNull;

public interface ConsumerListener<T> {

    void onConnected(@NonNull String stream, @NonNull String subject);

    void onError(@NonNull String stream, @NonNull String consumer, @NonNull Throwable throwable);
}
