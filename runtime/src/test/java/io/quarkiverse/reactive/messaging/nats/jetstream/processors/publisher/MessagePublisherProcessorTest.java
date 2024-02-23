package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import static io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher.MessagePublisherProcessor.createPushSubscribeOptions;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.Multi;

public class MessagePublisherProcessorTest {

    @Test
    void configureSubscriberClient() {
        final var durable = "durable";
        final var deleiverGroup = "deliver-group";
        final var backoff = new String[] { "PT1S" };
        final var maxDeliever = 3L;

        final var options = createPushSubscribeOptions(durable, deleiverGroup, backoff, maxDeliever);

        assertThat(options.getDurable()).isEqualTo(durable);
        assertThat(options.getDeliverGroup()).isEqualTo(deleiverGroup);
        assertThat(options.getConsumerConfiguration().getMaxDeliver()).isEqualTo(maxDeliever);
        assertThat(options.getConsumerConfiguration().getBackoff()).hasSize(1);
        assertThat(options.getConsumerConfiguration().getBackoff()).contains(Duration.ofSeconds(1));
}