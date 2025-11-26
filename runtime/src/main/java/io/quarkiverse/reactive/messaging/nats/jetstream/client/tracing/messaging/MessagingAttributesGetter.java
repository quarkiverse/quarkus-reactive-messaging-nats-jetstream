package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.messaging;

/**
 * An interface for getting messaging attributes.
 */
public interface MessagingAttributesGetter<T> {

    String getSystem(T request);

    String getDestination(T request);

    Long getMessageBodySize(T request);

    String getMessageId(T request);

}
