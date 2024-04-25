package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public interface JetStreamReaderConsumerConfiguration extends JetStreamPullConsumerConfiguration {

    Integer rePullAt();

    Integer maxRequestBatch();

}
