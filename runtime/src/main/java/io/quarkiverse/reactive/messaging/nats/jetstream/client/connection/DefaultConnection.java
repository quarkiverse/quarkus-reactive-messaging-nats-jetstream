package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;


import io.nats.client.*;
import io.nats.client.api.*;
import io.nats.client.impl.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.FetchConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerNotFoundException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.FetchException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.extern.jbosslog.JBossLog;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

@JBossLog
public record DefaultConnection(io.nats.client.Connection connection) implements Connection {

    @Override
    public @NonNull Uni<StreamInfo> streamInfo(@NonNull final String streamName) {
        return jetStreamManagement()
                .onItem().transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.getStreamInfo(streamName))))
                .onFailure().transform(ConnectionException::new);
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public @NonNull Multi<String> streamNames() {
        return jetStreamManagement()
                .onItem().transformToMulti(jetStreamManagement -> Multi.createFrom().items(Unchecked.supplier(() -> jetStreamManagement.getStreamNames().stream())))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Uni<PurgeResponse> purgeStream(@NonNull final String streamName) {
        return jetStreamManagement()
                .onItem().transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.purgeStream(streamName))))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Uni<Boolean> deleteMessage(@NonNull String streamName, long seq, boolean erase) {
        return jetStreamManagement()
                .onItem().transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.deleteMessage(streamName, seq, erase))))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Uni<StreamInfo> addStream(@NonNull StreamConfiguration config) {
        return jetStreamManagement()
                .onItem().transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.addStream(config))))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Uni<StreamInfo> updateStream(@NonNull StreamConfiguration config) {
        return jetStreamManagement()
                .onItem().transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.updateStream(config))))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Uni<List<String>> bucketNames() {
        return keyValueManagement()
                .onItem().transformToUni(keyValueManagement -> Uni.createFrom().item(Unchecked.supplier(keyValueManagement::getBucketNames)))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Uni<KeyValueStatus> addKeyValueStore(@NonNull KeyValueConfiguration config) {
        return keyValueManagement()
                .onItem().transformToUni(keyValueManagement -> Uni.createFrom().item(Unchecked.supplier(() -> keyValueManagement.create(config))))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Uni<KeyValue> keyValue(@NonNull final String bucketName) {
        return Uni.createFrom().item(Unchecked.supplier(() -> connection.keyValue(bucketName)))
                .onFailure().transform(ClientException::new);
    }

    @Override
    public @NonNull Uni<PublishAck> publish(String subject, Headers headers, byte[] body, PublishOptions options) {
        return jetStream()
                .onItem().transformToUni(jetStream -> Uni.createFrom().item(Unchecked.supplier(() -> jetStream.publish(subject, headers, body, options))))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Uni<io.nats.client.ConsumerContext> consumerContext(@NonNull final String stream, @NonNull final String consumer) {
        return streamContext(stream)
                .onItem().transformToUni(streamContext -> Uni.createFrom().item(Unchecked.supplier(() -> streamContext.getConsumerContext(consumer))))
                .onFailure().transform(failure -> {
                    if (failure instanceof JetStreamApiException) {
                        return new ConsumerNotFoundException(stream, consumer, failure);
                    } else {
                        return new ConnectionException(failure);
                    }
                });
    }

    @Override
    public @NonNull Uni<ConsumerInfo> consumerInfo(@NonNull String stream, @NonNull String consumer) {
        return jetStreamManagement()
                .onItem().transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.getConsumerNames(stream)))
                        .onItem().transformToUni(consumerNames -> {
                            if (consumerNames.contains(consumer)) {
                                return Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.getConsumerInfo(stream, consumer)));
                            } else {
                                return Uni.createFrom().nullItem();
                            }
                        }))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Uni<ConsumerInfo> createConsumer(@NonNull String stream, @NonNull ConsumerConfiguration configuration) {
        return jetStreamManagement()
                .onItem().transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.createConsumer(stream, configuration))))
                .onFailure().transform(ConnectionException::new);
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public @NonNull Multi<String> consumerNames(@NonNull String stream) {
        return jetStreamManagement().onItem().transformToMulti(jetStreamManagement ->
                Multi.createFrom().items(Unchecked.supplier(() -> jetStreamManagement.getConsumerNames(stream).stream()))
                        .onFailure().transform(ConnectionException::new));
    }

    @Override
    public @NonNull Uni<Boolean> deleteConsumer(@NonNull String stream, @NonNull String consumer) {
        return jetStreamManagement().onItem().transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.deleteConsumer(stream, consumer))))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Uni<ConsumerPauseResponse> pauseConsumer(@NonNull String streamName, @NonNull String consumerName, @NonNull ZonedDateTime pauseUntil) {
        return jetStreamManagement()
                .onItem().transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.pauseConsumer(streamName, consumerName, pauseUntil))))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Uni<Boolean> resumeConsumer(@NonNull String streamName, @NonNull String consumerName) {
        return jetStreamManagement()
                .onItem().transformToUni(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.resumeConsumer(streamName, consumerName))))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Uni<Message> next(@NonNull final String stream, @NonNull final String consumer, @NonNull final Duration timeout) {
        return consumerContext(stream, consumer)
                .onItem().transformToUni(consumerContext ->
                        Uni.createFrom().item(Unchecked.supplier(() -> consumerContext.next(timeout))))
                .onFailure().transform(ConnectionException::new);
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public @NonNull Multi<Message> fetch(@NonNull String stream, @NonNull String consumer, @NonNull FetchConfiguration configuration) {
        return consumerContext(stream, consumer)
                .onItem().transformToMulti(consumerContext -> Multi.createFrom().<io.nats.client.Message>emitter(emitter -> {
                    try {
                        try (final var fetchConsumer = fetchConsumer(consumerContext, configuration)) {
                            var message = fetchConsumer.nextMessage();
                            while (message != null) {
                                emitter.emit(message);
                                message = fetchConsumer.nextMessage();
                            }
                            emitter.complete();
                        }
                    } catch (Exception failure) {
                        emitter.fail(new FetchException(failure));
                    }
                }))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Uni<MessageInfo> resolve(@NonNull final String streamName, final long sequence) {
        return streamContext(streamName)
                .onItem().transformToUni(streamContxt -> Uni.createFrom().item(Unchecked.supplier(() -> streamContxt.getMessage(sequence))))
                .onFailure().transform(ConnectionException::new);
    }

    @Override
    public @NonNull Multi<Message> subscribe(@NonNull String stream, @NonNull String consumer, @NonNull PushConfiguration configuration) {
        return Multi.createFrom().<Message>emitter(emitter -> {
                    try {
                        final var jetStream = connection.jetStream();
                        final var dispatcher = connection.createDispatcher();
                        final var pushOptions = pushSubscribeOptions(stream, consumer, configuration.ordered());
                        jetStream.subscribe(
                                null, dispatcher,
                                emitter::emit,
                                false,
                                pushOptions);
                    } catch (Exception e) {
                        log.errorf(
                                e,
                                "Failed subscribing to stream: %s and consumer: %s with message: %s",
                                stream,
                                consumer,
                                e.getMessage());
                        emitter.fail(e);
                    }
                })
                .onFailure().transform(ConnectionException::new);
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public @NonNull Multi<Message> subscribe(@NonNull String stream, @NonNull String consumer, @NonNull PullConfiguration configuration) {
        if (configuration.batchSize() <= 1) {
            return consumerContext(stream, consumer)
                    .onItem().transformToMulti(consumerContext -> Multi.createBy().repeating()
                            .uni(() -> next(stream, consumerContext, configuration.maxExpires()))
                            .whilst(message -> true)
                            .flatMap(message -> message != null ? Multi.createFrom().item(message) : Multi.createFrom().empty())
                            .onFailure().transform(ConnectionException::new));
        } else {
            return reader(stream, consumer, configuration)
                    .onItem().transformToMulti(reader -> Multi.createBy().repeating()
                    .uni(() -> next(stream, reader, configuration.maxExpires()))
                    .whilst(message -> true)
                    .flatMap(message -> message != null ? Multi.createFrom().item(message) : Multi.createFrom().empty())
                    .onFailure().transform(ConnectionException::new));
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (Exception e) {
            log.warnf(e, "Failed to close connection with message: %s", e.getMessage());
        }
    }

    @Override
    public String getConnectedUrl() {
        return connection.getConnectedUrl();
    }

    private FetchConsumer fetchConsumer(final ConsumerContext consumerContext, final FetchConfiguration configuration)
            throws IOException, JetStreamApiException {
        if (configuration.timeout().isEmpty()) {
            return consumerContext.fetch(
                    FetchConsumeOptions.builder().maxMessages(configuration.batchSize()).noWait().build());
        } else {
            return consumerContext
                    .fetch(FetchConsumeOptions.builder().maxMessages(configuration.batchSize())
                            .expiresIn(configuration.timeout().get().toMillis()).build());
        }
    }

    private Uni<StreamContext> streamContext(@NonNull final String stream) {
        return Uni.createFrom().item(Unchecked.supplier(() -> connection.getStreamContext(stream)));
    }

    private Uni<JetStream> jetStream() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStream));
    }

    private Uni<JetStreamManagement> jetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection::jetStreamManagement));
    }

    private Uni<KeyValueManagement> keyValueManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection::keyValueManagement));
    }

    private @NonNull PushSubscribeOptions pushSubscribeOptions(@NonNull final String stream,
                                                               @NonNull final String consumer,
                                                               @NonNull Boolean ordered) {
        return PushSubscribeOptions.builder()
                .stream(stream)
                .name(consumer)
                .bind(true)
                .ordered(ordered)
                .build();
    }

    private @NonNull PullSubscribeOptions pullSubscribeOptions(@NonNull final String stream, @NonNull final String consumer) {
        return PullSubscribeOptions.builder()
                .stream(stream)
                .name(consumer)
                .bind(true)
                .build();
    }

    private @NonNull Uni<Message> next(@NonNull String stream, @NonNull ConsumerContext consumerContext, @NonNull Duration timeout) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                emitter.complete(consumerContext.next(timeout));
            } catch (JetStreamStatusException e) {
                emitter.fail(new ConnectionException(e));
            } catch (IllegalStateException | InterruptedException e) {
                emitter.complete(null);
            } catch (Exception exception) {
                emitter.fail(new ConnectionException(String.format("Error reading next message from stream: %s", stream), exception));
            }
        });
    }

    private @NonNull Uni<JetStreamReader> reader(@NonNull final String stream, @NonNull final String consumer, @NonNull final PullConfiguration configuration) {
        return jetStream()
                .onItem().transformToUni(jetStream -> Uni.createFrom().item(Unchecked.supplier(() -> jetStream.subscribe(null, pullSubscribeOptions(stream, consumer)))))
                .onItem().transformToUni(subscription -> Uni.createFrom().item(Unchecked.supplier(() -> subscription.reader(configuration.batchSize(), configuration.rePullAt()))))
                .onFailure().transform(ConnectionException::new);
    }

    private @NonNull Uni<Message> next(@NonNull String stream, @NonNull JetStreamReader reader, @NonNull Duration timeout) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                emitter.complete(reader.nextMessage(timeout));
            } catch (JetStreamStatusException e) {
                emitter.fail(new ConnectionException(e));
            } catch (IllegalStateException | InterruptedException e) {
                emitter.complete(null);
            } catch (Exception exception) {
                emitter.fail(new ConnectionException(String.format("Error reading next message from stream: %s", stream), exception));
            }
        });
    }
}
