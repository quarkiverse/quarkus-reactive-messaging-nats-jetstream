package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import io.nats.client.*;
import io.nats.client.JetStream;
import io.nats.client.api.*;

record JetStreamManagementDelegate(io.nats.client.JetStreamManagement delegate) implements JetStreamManagement {

    @Override
    public AccountStatistics getAccountStatistics() throws IOException, JetStreamApiException {
        return delegate.getAccountStatistics();
    }

    @Override
    public StreamInfo addStream(StreamConfiguration config) throws IOException, JetStreamApiException {
        return delegate.addStream(config);
    }

    @Override
    public StreamInfo updateStream(StreamConfiguration config) throws IOException, JetStreamApiException {
        return delegate.updateStream(config);
    }

    @Override
    public boolean deleteStream(String streamName) throws IOException, JetStreamApiException {
        return delegate.deleteStream(streamName);
    }

    @Override
    public StreamInfo getStreamInfo(String streamName) throws IOException, JetStreamApiException {
        return delegate.getStreamInfo(streamName);
    }

    @Override
    public StreamInfo getStreamInfo(String streamName, StreamInfoOptions options) throws IOException, JetStreamApiException {
        return delegate.getStreamInfo(streamName, options);
    }

    @Override
    public PurgeResponse purgeStream(String streamName) throws IOException, JetStreamApiException {
        return delegate.purgeStream(streamName);
    }

    @Override
    public PurgeResponse purgeStream(String streamName, PurgeOptions options) throws IOException, JetStreamApiException {
        return delegate.purgeStream(streamName, options);
    }

    @Override
    public ConsumerInfo addOrUpdateConsumer(String streamName, ConsumerConfiguration config)
            throws IOException, JetStreamApiException {
        return delegate.addOrUpdateConsumer(streamName, config);
    }

    @Override
    public ConsumerInfo createConsumer(String streamName, ConsumerConfiguration config)
            throws IOException, JetStreamApiException {
        return delegate.createConsumer(streamName, config);
    }

    @Override
    public ConsumerInfo updateConsumer(String streamName, ConsumerConfiguration config)
            throws IOException, JetStreamApiException {
        return delegate.updateConsumer(streamName, config);
    }

    @Override
    public boolean deleteConsumer(String streamName, String consumerName) throws IOException, JetStreamApiException {
        return delegate.deleteConsumer(streamName, consumerName);
    }

    @Override
    public ConsumerPauseResponse pauseConsumer(String streamName, String consumerName, ZonedDateTime pauseUntil)
            throws IOException, JetStreamApiException {
        return delegate.pauseConsumer(streamName, consumerName, pauseUntil);
    }

    @Override
    public boolean resumeConsumer(String streamName, String consumerName) throws IOException, JetStreamApiException {
        return delegate.resumeConsumer(streamName, consumerName);
    }

    @Override
    public ConsumerInfo getConsumerInfo(String streamName, String consumerName) throws IOException, JetStreamApiException {
        return delegate.getConsumerInfo(streamName, consumerName);
    }

    @Override
    public List<String> getConsumerNames(String streamName) throws IOException, JetStreamApiException {
        return delegate.getConsumerNames(streamName);
    }

    @Override
    public List<ConsumerInfo> getConsumers(String streamName) throws IOException, JetStreamApiException {
        return delegate.getConsumers(streamName);
    }

    @Override
    public List<String> getStreamNames() throws IOException, JetStreamApiException {
        return delegate.getStreamNames();
    }

    @Override
    public List<String> getStreamNames(String subjectFilter) throws IOException, JetStreamApiException {
        return delegate.getStreamNames(subjectFilter);
    }

    @Override
    public List<StreamInfo> getStreams() throws IOException, JetStreamApiException {
        return delegate.getStreams();
    }

    @Override
    public List<StreamInfo> getStreams(String subjectFilter) throws IOException, JetStreamApiException {
        return delegate.getStreams(subjectFilter);
    }

    @Override
    public MessageInfo getMessage(String streamName, long seq) throws IOException, JetStreamApiException {
        return delegate.getMessage(streamName, seq);
    }

    @Override
    public MessageInfo getMessage(String streamName, MessageGetRequest messageGetRequest)
            throws IOException, JetStreamApiException {
        return delegate.getMessage(streamName, messageGetRequest);
    }

    @Override
    public MessageInfo getLastMessage(String streamName, String subject) throws IOException, JetStreamApiException {
        return delegate.getLastMessage(streamName, subject);
    }

    @Override
    public MessageInfo getFirstMessage(String streamName, String subject) throws IOException, JetStreamApiException {
        return delegate.getFirstMessage(streamName, subject);
    }

    @Override
    public MessageInfo getFirstMessage(String streamName, ZonedDateTime startTime) throws IOException, JetStreamApiException {
        return delegate.getFirstMessage(streamName, startTime);
    }

    @Override
    public MessageInfo getFirstMessage(String streamName, ZonedDateTime startTime, String subject)
            throws IOException, JetStreamApiException {
        return delegate.getFirstMessage(streamName, startTime, subject);
    }

    @Override
    public MessageInfo getNextMessage(String streamName, long seq, String subject) throws IOException, JetStreamApiException {
        return delegate.getNextMessage(streamName, seq, subject);
    }

    @Override
    public boolean deleteMessage(String streamName, long seq) throws IOException, JetStreamApiException {
        return delegate.deleteMessage(streamName, seq);
    }

    @Override
    public boolean deleteMessage(String streamName, long seq, boolean erase) throws IOException, JetStreamApiException {
        return delegate.deleteMessage(streamName, seq, erase);
    }

    @Override
    public boolean unpinConsumer(String streamName, String consumerName, String consumerGroup)
            throws IOException, JetStreamApiException {
        return delegate.unpinConsumer(streamName, consumerName, consumerGroup);
    }

    @Override
    public ConsumerInfo resetConsumer(String streamName, String consumerName) throws IOException, JetStreamApiException {
        return delegate.resetConsumer(streamName, consumerName);
    }

    @Override
    public ConsumerInfo resetConsumer(String streamName, String consumerName, long sequence)
            throws IOException, JetStreamApiException {
        return delegate.resetConsumer(streamName, consumerName, sequence);
    }

    @Override
    public JetStream jetStream() {
        return delegate.jetStream();
    }

    @Override
    public io.nats.client.KeyValue keyValue(String bucketName) throws IOException {
        return delegate.keyValue(bucketName);
    }

    @Override
    public KeyValueManagement keyValueManagement() throws IOException {
        return delegate.keyValueManagement();
    }

    @Override
    public io.nats.client.ObjectStore objectStore(String bucketName) throws IOException {
        return delegate.objectStore(bucketName);
    }

    @Override
    public ObjectStoreManagement objectStoreManagement() throws IOException {
        return delegate.objectStoreManagement();
    }
}
