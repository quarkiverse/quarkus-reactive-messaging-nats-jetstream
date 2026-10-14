package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.nats.client.support.NatsKeyValueUtil.toKeyPrefix;
import static io.nats.client.support.NatsKeyValueUtil.trimPrefix;

import org.jspecify.annotations.NonNull;

import io.nats.client.api.Mirror;
import io.nats.client.api.StreamConfiguration;

/**
 * Resolves the subject a key-value write is published to, the same way JNats' {@code NatsKeyValue} does,
 * so that writes to mirrored buckets (including mirrors of buckets in other domains) reach the origin bucket.
 */
final class KeyValueWriteSubject {

    private KeyValueWriteSubject() {
    }

    static @NonNull String writePrefix(@NonNull String bucketName, @NonNull StreamConfiguration configuration) {
        final Mirror mirror = configuration.getMirror();
        if (mirror == null) {
            return toKeyPrefix(bucketName);
        }
        final String originBucketName = trimPrefix(mirror.getName());
        final String externalApi = mirror.getExternal() == null ? null : mirror.getExternal().getApi();
        if (externalApi == null) {
            return toKeyPrefix(originBucketName);
        }
        return externalApi + "." + toKeyPrefix(originBucketName);
    }

    static @NonNull String writeSubject(@NonNull String bucketName, @NonNull StreamConfiguration configuration,
            @NonNull String key) {
        return writePrefix(bucketName, configuration) + key;
    }
}
