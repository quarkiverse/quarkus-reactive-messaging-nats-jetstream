package io.quarkiverse.reactive.nats;

import io.quarkiverse.reactive.nats.connection.ConnectionListener;
import io.quarkiverse.reactive.nats.connection.ForceReconnectOptions;
import io.quarkiverse.reactive.nats.connection.Options;
import io.quarkiverse.reactive.nats.consumer.ConsumerContext;
import io.quarkiverse.reactive.nats.consumer.Dispatcher;
import io.quarkiverse.reactive.nats.consumer.Subscription;
import io.quarkiverse.reactive.nats.jetstream.JetStream;
import io.quarkiverse.reactive.nats.jetstream.JetStreamManagement;
import io.quarkiverse.reactive.nats.jetstream.JetStreamOptions;
import io.quarkiverse.reactive.nats.keyvalue.KeyValue;
import io.quarkiverse.reactive.nats.keyvalue.KeyValueManagement;
import io.quarkiverse.reactive.nats.keyvalue.KeyValueOptions;
import io.quarkiverse.reactive.nats.message.Headers;
import io.quarkiverse.reactive.nats.message.Message;
import io.quarkiverse.reactive.nats.message.MessageHandler;
import io.quarkiverse.reactive.nats.objectstore.ObjectStore;
import io.quarkiverse.reactive.nats.objectstore.ObjectStoreManagement;
import io.quarkiverse.reactive.nats.objectstore.ObjectStoreOptions;
import io.quarkiverse.reactive.nats.stream.StreamContext;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetAddress;
import java.time.Duration;

/**
 * @see io.nats.client.Connection
 */
public interface Client {

    /**
     * @see io.nats.client.Connection#publish(String, byte[])
     */
    @NonNull
    Uni<Void> publish(@NonNull String subject, byte @Nullable [] body);

    /**
     * @see io.nats.client.Connection#publish(String, io.nats.client.impl.Headers, byte[])
     */
    @NonNull
    Uni<Void> publish(@NonNull String subject, @Nullable Headers headers, byte @Nullable [] body);

    /**
     * @see io.nats.client.Connection#publish(String, String, byte[])
     */
    @NonNull
    Uni<Void> publish(@NonNull String subject, @Nullable String replyTo, byte @Nullable [] body);

    /**
     * @see io.nats.client.Connection#publish(String, String, io.nats.client.impl.Headers, byte[])
     */
    @NonNull
    Uni<Void> publish(@NonNull String subject, @Nullable String replyTo, @Nullable Headers headers, byte @Nullable [] body);

    /**
     * @see io.nats.client.Connection#publish(io.nats.client.Message)
     */
    @NonNull
    Uni<Void> publish(@NonNull Message message);

    /**
     * @see io.nats.client.Connection#request(String, byte[])
     */
    @NonNull
    Uni<Message> request(@NonNull String subject, byte @Nullable [] body);

    /**
     * @see io.nats.client.Connection#request(String, io.nats.client.impl.Headers, byte[])
     */
    @NonNull
    Uni<Message> request(@NonNull String subject, @Nullable Headers headers, byte @Nullable [] body);

    /**
     * @see io.nats.client.Connection#request(String, byte[], Duration)
     */
    @NonNull
    Uni<Message> requestWithTimeout(@NonNull String subject, byte @Nullable [] body, @Nullable Duration timeout);

    /**
     * @see io.nats.client.Connection#request(String, io.nats.client.impl.Headers, byte[], Duration)
     */
    @NonNull
    Uni<Message> requestWithTimeout(@NonNull String subject, @Nullable Headers headers, byte @Nullable [] body, Duration timeout);

    /**
     * @see io.nats.client.Connection#request(io.nats.client.Message)
     */
    @NonNull
    Uni<Message> request(@NonNull Message message);

    /**
     * @see io.nats.client.Connection#requestWithTimeout(io.nats.client.Message, Duration)
     */
    @NonNull
    Uni<Message> requestWithTimeout(@NonNull Message message, @Nullable Duration timeout);

    /**
     * @see io.nats.client.Connection#request(String, byte[], Duration)
     */
    @NonNull
    Uni<Message> request(@NonNull String subject, byte @Nullable [] body, @Nullable Duration timeout);

    /**
     * @see io.nats.client.Connection#request(String, io.nats.client.impl.Headers, byte[], Duration)
     */
    @NonNull
    Uni<Message> request(@NonNull String subject, @Nullable Headers headers, byte @Nullable [] body, @Nullable Duration timeout);

    /**
     * @see io.nats.client.Connection#request(io.nats.client.Message, Duration)
     */
    @NonNull
    Uni<Message> request(@NonNull Message message, @Nullable Duration timeout);

    /**
     * @see io.nats.client.Connection#subscribe(String)
     */
    @NonNull
    Uni<Subscription> subscribe(@NonNull String subject);

    /**
     * @see io.nats.client.Connection#subscribe(String, String)
     */
    @NonNull
    Uni<Subscription> subscribe(@NonNull String subject, @NonNull String queueName);

    /**
     * @see io.nats.client.Connection#createDispatcher(io.nats.client.MessageHandler)
     */
    @NonNull
    Uni<Dispatcher> createDispatcher(@Nullable MessageHandler handler);

    /**
     * @see io.nats.client.Connection#createDispatcher()
     */
    @NonNull
    Uni<Dispatcher> createDispatcher();

    /**
     * @see io.nats.client.Connection#closeDispatcher(io.nats.client.Dispatcher)
     */
    @NonNull
    Uni<Void> closeDispatcher(@NonNull Dispatcher dispatcher);

    /**
     * @see io.nats.client.Connection#addConnectionListener(io.nats.client.ConnectionListener)
     */
    @NonNull
    Uni<Void> addConnectionListener(@NonNull ConnectionListener connectionListener);

    /**
     * @see io.nats.client.Connection#removeConnectionListener(io.nats.client.ConnectionListener)
     */
    @NonNull
    Uni<Void> removeConnectionListener(@NonNull ConnectionListener connectionListener);

    /**
     * @see io.nats.client.Connection#flush(java.time.Duration)
     */
    @NonNull
    Uni<Void> flush(@Nullable Duration timeout);

    /**
     * @see io.nats.client.Connection#drain(java.time.Duration)
     */
    @NonNull
    Uni<Boolean> drain(@Nullable Duration timeout);

    /**
     * @see io.nats.client.Connection#close()
     */
    @NonNull
    Uni<Void> close();

    /**
     * Returns the client's current status.
     *
     * @return the client's status
     */
    @NonNull
    Uni<Status> getStatus();

    /**
     * @see io.nats.client.Connection#getMaxPayload()
     */
    @NonNull
    Uni<Long> getMaxPayload();

    /**
     * @see io.nats.client.Connection#getServers()
     */
    @NonNull
    Multi<String> getServers();

    /**
     * @see io.nats.client.Connection#getStatistics()
     */
    @NonNull
    Uni<Statistics> getStatistics();

    /**
     * @see io.nats.client.Connection#getOptions()
     */
    @NonNull
    Uni<Options> getOptions();

    /**
     * @see io.nats.client.Connection#getServerInfo()
     */
    @NonNull
    Uni<ServerInfo> getServerInfo();

    /**
     * @see io.nats.client.Connection#getConnectedUrl()
     */
    @NonNull
    Uni<String> getConnectedUrl();

    /**
     * @see io.nats.client.Connection#getClientInetAddress()
     */
    @NonNull
    Uni<InetAddress> getClientInetAddress();

    /**
     * @see io.nats.client.Connection#getLastError()
     */
    @NonNull
    Uni<String> getLastError();

    /**
     * @see io.nats.client.Connection#clearLastError()
     */
    @NonNull
    Uni<Void> clearLastError();

    /**
     * @see io.nats.client.Connection#createInbox()
     */
    @NonNull
    Uni<String> createInbox();

    /**
     * @see io.nats.client.Connection#flushBuffer()
     */
    @NonNull
    Uni<Void> flushBuffer();

    /**
     * @see io.nats.client.Connection#forceReconnect()
     */
    @NonNull
    Uni<Void> forceReconnect();

    /**
     * @see io.nats.client.Connection#forceReconnect(io.nats.client.ForceReconnectOptions)
     */
    @NonNull
    Uni<Void> forceReconnect(@Nullable ForceReconnectOptions options);

    /**
     * @see io.nats.client.Connection#RTT()
     */
    @NonNull
    Uni<Duration> RTT();

    /**
     * @see io.nats.client.Connection#getStreamContext(String)
     */
    @NonNull
    Uni<StreamContext> getStreamContext(@NonNull String streamName);

    /**
     * @see io.nats.client.Connection#getStreamContext(String, io.nats.client.JetStreamOptions)
     */
    @NonNull
    Uni<StreamContext> getStreamContext(@NonNull String streamName, @Nullable JetStreamOptions options);

    /**
     * @see io.nats.client.Connection#getConsumerContext(String, String)
     */
    @NonNull
    Uni<ConsumerContext> getConsumerContext(@NonNull String streamName, @NonNull String consumerName);

    /**
     * @see io.nats.client.Connection#getConsumerContext(String, String, io.nats.client.JetStreamOptions)
     */
    @NonNull
    Uni<ConsumerContext> getConsumerContext(@NonNull String streamName, @NonNull String consumerName, @Nullable JetStreamOptions options);

    /**
     * @see io.nats.client.Connection#jetStream()
     */
    @NonNull
    Uni<JetStream> jetStream();

    /**
     * @see io.nats.client.Connection#jetStream(io.nats.client.JetStreamOptions)
     */
    @NonNull
    Uni<JetStream> jetStream(@Nullable JetStreamOptions options);

    /**
     * @see io.nats.client.Connection#jetStreamManagement()
     */
    @NonNull
    Uni<JetStreamManagement> jetStreamManagement();

    /**
     * @see io.nats.client.Connection#jetStreamManagement(io.nats.client.JetStreamOptions)
     */
    @NonNull
    Uni<JetStreamManagement> jetStreamManagement(@Nullable JetStreamOptions options);

    /**
     * @see io.nats.client.Connection#keyValue(String)
     */
    @NonNull
    Uni<KeyValue> keyValue(@NonNull String bucketName);

    /**
     * @see io.nats.client.Connection#keyValue(String, io.nats.client.KeyValueOptions)
     */
    @NonNull
    Uni<KeyValue> keyValue(@NonNull String bucketName, @Nullable KeyValueOptions options);

    /**
     * @see io.nats.client.Connection#keyValueManagement()
     */
    @NonNull
    Uni<KeyValueManagement> keyValueManagement();

    /**
     * @see io.nats.client.Connection#keyValueManagement(io.nats.client.KeyValueOptions)
     */
    @NonNull
    Uni<KeyValueManagement> keyValueManagement(@Nullable KeyValueOptions options);

    /**
     * @see io.nats.client.Connection#objectStore(String)
     */
    @NonNull
    Uni<ObjectStore> objectStore(@NonNull String bucketName);

    /**
     * @see io.nats.client.Connection#objectStore(String, io.nats.client.ObjectStoreOptions)
     */
    @NonNull
    Uni<ObjectStore> objectStore(@NonNull String bucketName, @Nullable ObjectStoreOptions options);

    /**
     * @see io.nats.client.Connection#objectStoreManagement()
     */
    @NonNull
    Uni<ObjectStoreManagement> objectStoreManagement();

    /**
     * @see io.nats.client.Connection#objectStoreManagement(io.nats.client.ObjectStoreOptions)
     */
    @NonNull
    Uni<ObjectStoreManagement> objectStoreManagement(ObjectStoreOptions options);

    /**
     * Get the number of messages in the outgoing queue for this connection.
     * This value is volatile in the sense that it changes often and may be adjusted by more than one message.
     * It changes every time a message is published (put in the outgoing queue)
     * and every time a message is removed from the queue to be written over the socket
     *
     * @return the number of messages in the outgoing queue
     */
    @NonNull
    Uni<Long> outgoingPendingMessageCount();

    /**
     * Get the number of bytes based to be written calculated from the messages in the outgoing queue for this connection.
     * This value is volatile in the sense that it changes often and may be adjusted by more than one message's bytes.
     * It changes every time a message is published (put in the outgoing queue)
     * and every time a message is removed from the queue to be written over the socket
     *
     * @return the number of messages in the outgoing queue
     */
    @NonNull
    Uni<Long> outgoingPendingBytes();

}
