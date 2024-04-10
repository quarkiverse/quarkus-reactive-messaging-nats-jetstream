package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

public class MessagePublisherProcessorTest {

    @Test
    void configureSubscriberClient() {
        final var durable = "durable";
        final var deleiverGroup = "deliver-group";
        final var backoff = new String[] { "PT1S" };
        final var maxDeliever = 3L;

        final var factory = new PushSubscribeOptionsFactory();
        final var options = factory.create(durable, deleiverGroup, backoff, maxDeliever, null);

        assertThat(options.getDurable()).isEqualTo(durable);
        assertThat(options.getDeliverGroup()).isEqualTo(deleiverGroup);
        assertThat(options.getConsumerConfiguration().getMaxDeliver()).isEqualTo(maxDeliever);
        assertThat(options.getConsumerConfiguration().getBackoff()).hasSize(1);
        assertThat(options.getConsumerConfiguration().getBackoff()).contains(Duration.ofSeconds(1));
    }
}
