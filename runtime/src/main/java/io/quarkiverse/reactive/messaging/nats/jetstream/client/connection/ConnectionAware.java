package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.DefaultErrorListener;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

public interface ConnectionAware {
    Logger logger = Logger.getLogger(ConnectionAware.class);

    ConnectionFactory connectionFactory();

    default <T> Uni<T> withConnection(ConnectionConsumer<T> consumer) {
        return withConnection(new DefaultConnectionListener(new DefaultErrorListener()), consumer);
    }

    default <T> Uni<T> withConnection(ConnectionListener listener, ConnectionConsumer<T> consumer) {
        return connectionFactory().create()
                .onItem().invoke(listener::onConnected)
                .onItem().transformToUni(connection -> consumer.accept(connection)
                        .onItem().invoke(connection::close)
                        .onFailure().invoke(listener::onError)
                        .onFailure().invoke(connection::close));
    }

    default <T> Multi<T> withSubscriberConnection(SubscriberConnectionConsumer<T> consumer) {
        return withSubscriberConnection(new DefaultConnectionListener(new DefaultErrorListener()), consumer);

    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    default <T> Multi<T> withSubscriberConnection(ConnectionListener listener, SubscriberConnectionConsumer<T> consumer) {
        return connectionFactory().create()
                .onItem().invoke(listener::onConnected)
                .onItem().transformToMulti(connection -> consumer.accept(connection)
                        .onCompletion().invoke(connection::close)
                        .onTermination().invoke(connection::close)
                        .onCancellation().invoke(connection::close)
                        .onFailure().invoke(listener::onError)
                        .onFailure().invoke(connection::close));

    }
}
