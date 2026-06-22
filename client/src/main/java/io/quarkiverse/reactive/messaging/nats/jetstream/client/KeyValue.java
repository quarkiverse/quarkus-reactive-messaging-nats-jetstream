package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueEntry;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStatus;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;

public interface KeyValue {

    @NonNull String bucketName();

    @NonNull Uni<KeyValueEntry> get(@NonNull String key);

    @NonNull Uni<KeyValueEntry> get(@NonNull String key, long revision);

    @NonNull Uni<KeyValueEntry> put(@NonNull String key, byte[] value);

    @NonNull Uni<Void> delete(@NonNull String key);

    @NonNull Uni<Void> delete(@NonNull String key, long expectedRevision);

    @NonNull Uni<Void> purge(@NonNull String key);

    @NonNull Uni<Void> purge(@NonNull String key, long expectedRevision);

    @NonNull Multi<String> keys();

    @NonNull Uni<KeyValueStatus> status();
}
