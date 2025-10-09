package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface PublisherAware {

    <T> Uni<Message<T>> publish(Message<T> message, String stream, String subject);

    <T> Uni<Message<T>> publish(Message<T> message, String stream, String subject, PublishListener listener);

    <T> Multi<Message<T>> publish(Multi<Message<T>> messages, String stream, String subject);

    <T> Multi<Message<T>> publish(Multi<Message<T>> messages, String stream, String subject, PublishListener listener);

}
