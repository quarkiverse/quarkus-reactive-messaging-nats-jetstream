package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.io.IOException;
import java.time.Duration;

import io.nats.client.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.smallrye.mutiny.Uni;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
class PullMessageReader extends AbstractConsumer implements PullMessage {
    private final String stream;
    private final JetStreamReader reader;
    private final JetStreamSubscription subscription;
    private final PullConsumerConfiguration consumerConfiguration;

    PullMessageReader(final JetStream jetStream,
            final String stream,
            final String consumer,
            final PullConsumerConfiguration consumerConfiguration) throws IOException, JetStreamApiException {
        this.stream = stream;
        this.consumerConfiguration = consumerConfiguration;
        PullSubscribeOptions options = createOptions(stream, consumer, consumerConfiguration);
        this.subscription = jetStream.subscribe(null, options);
        this.reader = subscription.reader(consumerConfiguration.pullConfiguration().batchSize(),
                consumerConfiguration.pullConfiguration().rePullAt());
    }

    @Override
    public Uni<Message> next() {
        return Uni.createFrom().<io.nats.client.Message> emitter(emitter -> {
            try {
                emitter.complete(reader.nextMessage(consumerConfiguration.pullConfiguration().maxExpires()));
            } catch (JetStreamStatusException e) {
                emitter.fail(new PullException(e));
            } catch (IllegalStateException | InterruptedException e) {
                emitter.complete(null);
            } catch (Exception exception) {
                emitter.fail(new PullException(String.format("Error reading next message from stream: %s", stream), exception));
            }
        });
    }

    @Override
    public void close() {
        try {
            reader.stop();
        } catch (Exception e) {
            log.warnf("Failed to stop reader with message %s", e.getMessage());
        }
        try {
            if (subscription.isActive()) {
                subscription.drain(Duration.ofMillis(1000));
            }
        } catch (Exception e) {
            log.warnf("Interrupted while draining subscription");
        }
    }

    private PullSubscribeOptions createOptions(final String stream, final String consumer,
            final PullConsumerConfiguration consumerConfiguration) {
        var builder = PullSubscribeOptions.builder();
        builder = builder.stream(stream);
        builder = builder.configuration(createConsumerConfiguration(consumer, consumerConfiguration));
        return builder.build();
    }
}
