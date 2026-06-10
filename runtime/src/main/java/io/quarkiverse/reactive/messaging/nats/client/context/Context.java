package io.quarkiverse.reactive.messaging.nats.client.context;

import io.smallrye.common.annotation.CheckReturnValue;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface Context extends io.smallrye.mutiny.vertx.MutinyDelegate {

    @Override
    io.vertx.core.Context getDelegate();

    @CheckReturnValue
    <T> Uni<T> execute(Uni<T> codeHandler);

    @CheckReturnValue
    <T> Multi<T> execute(Multi<T> codeHandler);

    void runOnContext(java.lang.Runnable action);
}
