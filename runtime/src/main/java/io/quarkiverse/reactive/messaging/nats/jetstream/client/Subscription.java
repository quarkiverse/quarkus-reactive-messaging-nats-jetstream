package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.Tracer;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.Context;

public interface Subscription<T> extends ConnectionListener {

    Multi<Message<T>> subscribe(Tracer<T> tracer, Context context);

}
