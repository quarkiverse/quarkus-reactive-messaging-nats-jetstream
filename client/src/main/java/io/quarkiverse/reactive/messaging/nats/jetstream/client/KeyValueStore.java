package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueEntry;
import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;

public interface KeyValueStore {

    @NonNull Uni<KeyValueEntry> getKeyValue(@NonNull String bucketName, @NonNull String key);

    @NonNull Uni<KeyValueEntry> getKeyValue(@NonNull String bucketName, @NonNull String key, long revision);

    @NonNull Uni<KeyValueEntry> putKeyValue(@NonNull String bucketName, @NonNull String key, byte[] value);

    @NonNull Uni<Void> deleteKeyValue(@NonNull String bucketName, @NonNull String key);

    @NonNull Uni<Void> deleteKeyValue(@NonNull String bucketName, @NonNull String key, long expectedRevision);

    @NonNull Uni<Void> purgeKeyValue(@NonNull String bucketName, @NonNull String key);

    @NonNull Uni<Void> purgeKeyValue(@NonNull String bucketName, @NonNull String key, long expectedRevision);

}
