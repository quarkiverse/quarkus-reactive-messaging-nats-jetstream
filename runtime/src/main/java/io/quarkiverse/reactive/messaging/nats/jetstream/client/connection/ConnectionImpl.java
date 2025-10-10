package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.nats.client.*;
import io.nats.client.api.ServerInfo;
import io.nats.client.impl.Headers;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public record ConnectionImpl(io.nats.client.Connection delegate) implements Connection {

    @Override
    public void publish(@NonNull String subject, byte @Nullable [] body) {
        delegate.publish(subject, body);
    }

    @Override
    public void publish(@NonNull String subject, @Nullable Headers headers, byte @Nullable [] body) {
        delegate.publish(subject, headers, body);
    }

    @Override
    public void publish(@NonNull String subject, @Nullable String replyTo, byte @Nullable [] body) {
        delegate.publish(subject, replyTo, body);
    }

    @Override
    public void publish(@NonNull String subject, @Nullable String replyTo, @Nullable Headers headers, byte @Nullable [] body) {
        delegate.publish(subject, replyTo, headers, body);
    }

    @Override
    public void publish(@NonNull Message message) {
        delegate.publish(message);
    }

    @Override
    public @NonNull CompletableFuture<Message> request(@NonNull String subject, byte @Nullable [] body) {
        return delegate.request(subject, body);
    }

    @Override
    public @NonNull CompletableFuture<Message> request(@NonNull String subject, @Nullable Headers headers,
            byte @Nullable [] body) {
        return delegate.request(subject, headers, body);
    }

    @Override
    public @NonNull CompletableFuture<Message> requestWithTimeout(@NonNull String subject, byte @Nullable [] body,
            @Nullable Duration timeout) {
        return delegate.requestWithTimeout(subject, body, timeout);
    }

    @Override
    public @NonNull CompletableFuture<Message> requestWithTimeout(@NonNull String subject, @Nullable Headers headers,
            byte @Nullable [] body, Duration timeout) {
        return delegate.requestWithTimeout(subject, headers, body, timeout);
    }

    @Override
    public @NonNull CompletableFuture<Message> request(@NonNull Message message) {
        return delegate.request(message);
    }

    @Override
    public @NonNull CompletableFuture<Message> requestWithTimeout(@NonNull Message message, @Nullable Duration timeout) {
        return delegate.requestWithTimeout(message, timeout);
    }

    @Override
    public @Nullable Message request(@NonNull String subject, byte @Nullable [] body, @Nullable Duration timeout)
            throws InterruptedException {
        return delegate.request(subject, body, timeout);
    }

    @Override
    public @Nullable Message request(@NonNull String subject, @Nullable Headers headers, byte @Nullable [] body,
            @Nullable Duration timeout) throws InterruptedException {
        return delegate.request(subject, headers, body, timeout);
    }

    @Override
    public @Nullable Message request(@NonNull Message message, @Nullable Duration timeout) throws InterruptedException {
        return delegate.request(message, timeout);
    }

    @Override
    public @NonNull Subscription subscribe(@NonNull String subject) {
        return delegate.subscribe(subject);
    }

    @Override
    public @NonNull Subscription subscribe(@NonNull String subject, @NonNull String queueName) {
        return delegate.subscribe(subject, queueName);
    }

    @Override
    public @NonNull Dispatcher createDispatcher(@Nullable MessageHandler handler) {
        return delegate.createDispatcher(handler);
    }

    @Override
    public @NonNull Dispatcher createDispatcher() {
        return delegate.createDispatcher();
    }

    @Override
    public void closeDispatcher(@NonNull Dispatcher dispatcher) {
        delegate.closeDispatcher(dispatcher);
    }

    @Override
    public void addConnectionListener(@NonNull ConnectionListener connectionListener) {
        delegate.addConnectionListener(connectionListener);
    }

    @Override
    public void removeConnectionListener(@NonNull ConnectionListener connectionListener) {
        delegate.removeConnectionListener(connectionListener);
    }

    @Override
    public void flush(@Nullable Duration timeout) throws TimeoutException, InterruptedException {
        delegate.flush(timeout);
    }

    @Override
    public @NonNull CompletableFuture<Boolean> drain(@Nullable Duration timeout) throws TimeoutException, InterruptedException {
        return delegate.drain(timeout);
    }

    @Override
    public void close() throws InterruptedException {
        delegate.close();
    }

    @Override
    public @NonNull Status getStatus() {
        return delegate.getStatus();
    }

    @Override
    public long getMaxPayload() {
        return delegate.getMaxPayload();
    }

    @Override
    public @NonNull Collection<String> getServers() {
        return delegate.getServers();
    }

    @Override
    public @NonNull Statistics getStatistics() {
        return delegate.getStatistics();
    }

    @Override
    public @NonNull Options getOptions() {
        return delegate.getOptions();
    }

    @Override
    public @NonNull ServerInfo getServerInfo() {
        return delegate.getServerInfo();
    }

    @Override
    public @Nullable String getConnectedUrl() {
        return delegate.getConnectedUrl();
    }

    @Override
    public @Nullable InetAddress getClientInetAddress() {
        return delegate.getClientInetAddress();
    }

    @Override
    public @Nullable String getLastError() {
        return delegate.getLastError();
    }

    @Override
    public void clearLastError() {
        delegate.clearLastError();
    }

    @Override
    public @NonNull String createInbox() {
        return delegate.createInbox();
    }

    @Override
    public void flushBuffer() throws IOException {
        delegate.flushBuffer();
    }

    @Override
    public void forceReconnect() throws IOException, InterruptedException {
        delegate.forceReconnect();
    }

    @Override
    public void forceReconnect(@Nullable ForceReconnectOptions options) throws IOException, InterruptedException {
        delegate.forceReconnect(options);
    }

    @Override
    public @NonNull Duration RTT() throws IOException {
        return delegate.RTT();
    }

    @Override
    public @NonNull StreamContext getStreamContext(@NonNull String streamName) throws IOException, JetStreamApiException {
        return delegate.getStreamContext(streamName);
    }

    @Override
    public @NonNull StreamContext getStreamContext(@NonNull String streamName, @Nullable JetStreamOptions options)
            throws IOException, JetStreamApiException {
        return delegate.getStreamContext(streamName, options);
    }

    @Override
    public @NonNull ConsumerContext getConsumerContext(@NonNull String streamName, @NonNull String consumerName)
            throws IOException, JetStreamApiException {
        return delegate.getConsumerContext(streamName, consumerName);
    }

    @Override
    public @NonNull ConsumerContext getConsumerContext(@NonNull String streamName, @NonNull String consumerName,
            @Nullable JetStreamOptions options) throws IOException, JetStreamApiException {
        return delegate.getConsumerContext(streamName, consumerName, options);
    }

    @Override
    public @NonNull JetStream jetStream() throws IOException {
        return delegate.jetStream();
    }

    @Override
    public @NonNull JetStream jetStream(@Nullable JetStreamOptions options) throws IOException {
        return delegate.jetStream(options);
    }

    @Override
    public @NonNull JetStreamManagement jetStreamManagement() throws IOException {
        return delegate.jetStreamManagement();
    }

    @Override
    public @NonNull JetStreamManagement jetStreamManagement(@Nullable JetStreamOptions options) throws IOException {
        return delegate.jetStreamManagement();
    }

    @Override
    public @NonNull KeyValue keyValue(@NonNull String bucketName) throws IOException {
        return delegate.keyValue(bucketName);
    }

    @Override
    public @NonNull KeyValue keyValue(@NonNull String bucketName, @Nullable KeyValueOptions options) throws IOException {
        return delegate.keyValue(bucketName, options);
    }

    @Override
    public @NonNull KeyValueManagement keyValueManagement() throws IOException {
        return delegate.keyValueManagement();
    }

    @Override
    public @NonNull KeyValueManagement keyValueManagement(@Nullable KeyValueOptions options) throws IOException {
        return delegate.keyValueManagement();
    }

    @Override
    public @NonNull ObjectStore objectStore(@NonNull String bucketName) throws IOException {
        return delegate.objectStore(bucketName);
    }

    @Override
    public @NonNull ObjectStore objectStore(@NonNull String bucketName, @Nullable ObjectStoreOptions options)
            throws IOException {
        return delegate.objectStore(bucketName, options);
    }

    @Override
    public @NonNull ObjectStoreManagement objectStoreManagement() throws IOException {
        return delegate.objectStoreManagement();
    }

    @Override
    public @NonNull ObjectStoreManagement objectStoreManagement(ObjectStoreOptions options) throws IOException {
        return delegate.objectStoreManagement(options);
    }

    @Override
    public long outgoingPendingMessageCount() {
        return delegate.outgoingPendingMessageCount();
    }

    @Override
    public long outgoingPendingBytes() {
        return delegate.outgoingPendingMessageCount();
    }
}
