package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

public interface JetStreamReaderConsumerConfiguration extends JetStreamPullConsumerConfiguration {

    String subject();

    Integer rePullAt();

    Integer maxRequestBatch();

}
