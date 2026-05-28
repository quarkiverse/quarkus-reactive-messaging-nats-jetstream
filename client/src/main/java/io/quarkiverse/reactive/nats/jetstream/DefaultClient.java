package io.quarkiverse.reactive.nats.jetstream;

import io.quarkiverse.reactive.nats.jetstream.message.Headers;
import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.quarkiverse.reactive.nats.jetstream.publish.PublishAcknowledge;
import io.quarkiverse.reactive.nats.jetstream.publish.PublishOptions;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record DefaultClient(NativeJetStream jetStream, Context context) implements Client {

    @Override
    public @NonNull Uni<PublishAcknowledge> publish(@NonNull String subject, byte @Nullable [] body) {
        return Uni.createFrom().future(Unchecked.supplier(() -> jetStream.publishAsync(subject, body)))
                .map(PublishAcknowledge::of)
                .emitOn(context::runOnContext);
    }

    @Override
    public @NonNull Uni<PublishAcknowledge> publish(@NonNull String subject, @Nullable Headers headers, byte @Nullable [] body) {
        return Uni.createFrom().future(Unchecked.supplier(() -> jetStream.publishAsync(subject, toHeaders(headers), body)))
                .map(PublishAcknowledge::of)
                .emitOn(context::runOnContext);
    }

    @Override
    public @NonNull Uni<PublishAcknowledge> publish(@NonNull String subject, byte @Nullable [] body, @Nullable PublishOptions options) {
        return Uni.createFrom().future(Unchecked.supplier(() -> jetStream.publishAsync(subject, body, toPublishOptions(options))))
                .map(PublishAcknowledge::of)
                .emitOn(context::runOnContext);
    }

    @Override
    public @NonNull Uni<PublishAcknowledge> publish(@NonNull String subject, @Nullable Headers headers, byte @Nullable [] body, @Nullable PublishOptions options) {
        return Uni.createFrom().future(Unchecked.supplier(() -> jetStream.publishAsync(subject, toHeaders(headers), body, toPublishOptions(options))))
                .map(PublishAcknowledge::of)
                .emitOn(context::runOnContext);
    }

    @Override
    public @NonNull Uni<PublishAcknowledge> publish(@NonNull Message message) {
        return null;
    }

    @Override
    public @NonNull Uni<PublishAcknowledge> publish(@NonNull Message message, @Nullable PublishOptions options) {
        return null;
    }

    private io.nats.client.impl.@NonNull Headers toHeaders(@Nullable Headers headers) {
        final var natsHeaders = new io.nats.client.impl.Headers();
        if (headers != null) {
             headers.entrySet().forEach(entry -> natsHeaders.add(entry.getKey(), entry.getValue()));
        }
        return natsHeaders;
    }

    private io.nats.client.@NonNull PublishOptions toPublishOptions(@Nullable PublishOptions options) {

    }
}
