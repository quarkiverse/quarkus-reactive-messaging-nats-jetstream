package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

public interface ReaderConsumerConfiguration<T> extends PullConsumerConfiguration<T> {

    String subject();

    Integer rePullAt();

    Integer maxRequestBatch();

}
