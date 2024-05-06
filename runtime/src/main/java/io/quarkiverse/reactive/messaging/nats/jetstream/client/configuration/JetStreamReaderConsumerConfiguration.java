package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

public interface JetStreamReaderConsumerConfiguration extends JetStreamPullConsumerConfiguration {

    Integer rePullAt();

    Integer maxRequestBatch();

}
