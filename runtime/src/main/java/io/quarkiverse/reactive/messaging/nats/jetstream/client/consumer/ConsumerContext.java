package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Duration;
import java.time.ZonedDateTime;

public interface ConsumerContext {

    /**
     * Adds a consumer
     *
     * @param stream The name of the stream
     * @param name The name of the consumer
     * @param configuration The consumer configuration
     * @return The consumer
     */
     <T> Uni<Consumer> addIfAbsent( String stream,  String name,  ConsumerConfiguration<T> configuration);

     <T> Uni<Consumer> addIfAbsent( String stream,  String name,  PushConsumerConfiguration<T> configuration);

     <T> Uni<Consumer> addIfAbsent( String stream,  String name,  PullConsumerConfiguration<T> configuration);

     Uni<Consumer> get( String stream,  String consumerName);

     Multi<String> names( String streamName);

     Uni<Void> delete( String streamName,  String consumerName);

     Uni<Void> pause( String streamName,  String consumerName,  ZonedDateTime pauseUntil);

     Uni<Void> resume( String streamName,  String consumerName);

     <T> Uni<Message<T>> next( String stream,  String consumer,  ConsumerConfiguration<T> configuration,  Duration timeout);

     <T> Multi<Message<T>> fetch( String stream,  String consumer,  FetchConsumerConfiguration<T> configuration);

     <T> Uni<Message<T>> resolve( String stream, long sequence);

     <T> Multi<Message<T>> subscribe( String stream,  String consumer,  PullConsumerConfiguration<T> configuration);

     <T> Multi<Message<T>> subscribe( String stream,  String consumer,  PushConsumerConfiguration<T> configuration);

}
