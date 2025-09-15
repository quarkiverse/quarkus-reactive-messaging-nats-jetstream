package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import io.nats.client.ConsumerContext;
import io.nats.client.KeyValue;
import io.nats.client.Message;
import io.nats.client.PublishOptions;
import io.nats.client.api.*;
import io.nats.client.impl.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.FetchConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConfiguration;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

public interface Connection {

    Uni<StreamInfo> streamInfo(final String streamName);

    Multi<String> streamNames();

    Uni<PurgeResponse> purgeStream(String streamName);

    Uni<Boolean> deleteMessage(String streamName, long seq, boolean erase);

    Uni<StreamInfo> addStream(StreamConfiguration config);

    Uni<StreamInfo> updateStream(StreamConfiguration config);

    Uni<List<String>> bucketNames();

    Uni<KeyValueStatus> addKeyValueStore(KeyValueConfiguration config);

    Uni<KeyValue> keyValue(String bucketName);

    Uni<PublishAck> publish(String subject, Headers headers, byte[] body, PublishOptions options);

    Uni<ConsumerContext> consumerContext(String stream, String consumer);

    Uni<ConsumerInfo> consumerInfo(String stream, String consumer);

    Uni<ConsumerInfo> createConsumer(String stream, ConsumerConfiguration configuration);

    Multi<String> consumerNames(String stream);

    Uni<Boolean> deleteConsumer(String stream, String consumer);

    Uni<ConsumerPauseResponse> pauseConsumer(String streamName, String consumerName, ZonedDateTime pauseUntil);

    Uni<Boolean> resumeConsumer(String streamName, String consumerName);

    Uni<Message> next(String stream, String consumer, Duration timeout);

    Multi<Message> fetch(String stream, String consumer, FetchConfiguration configuration);

    Uni<MessageInfo> resolve(String streamName, long sequence);

    Multi<Message> subscribe(String stream, String consumer, PushConfiguration configuration);

    Multi<Message> subscribe(String stream, String consumer, PullConfiguration configuration);

    void close();

    String getConnectedUrl();
}
