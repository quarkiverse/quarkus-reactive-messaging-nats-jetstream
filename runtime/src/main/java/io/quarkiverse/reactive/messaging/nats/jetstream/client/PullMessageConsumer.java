package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.nats.client.ConsumerContext;
import io.nats.client.JetStreamStatusException;
import io.nats.client.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.smallrye.mutiny.Uni;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class PullMessageConsumer extends AbstractConsumer implements PullMessage {
    private final String stream;
    private final PullConsumerConfiguration consumerConfiguration;
    private final ConsumerContext consumerContext;

    public PullMessageConsumer(String stream, PullConsumerConfiguration consumerConfiguration,
            ConsumerContext consumerContext) {
        this.stream = stream;
        this.consumerConfiguration = consumerConfiguration;
        this.consumerContext = consumerContext;
    }

    @Override
    public Uni<Message> next() {
        return Uni.createFrom().emitter(emitter -> {
            try {
                var maxExpires = consumerConfiguration.pullConfiguration().maxExpires();
                if (maxExpires != null) {
                    emitter.complete(consumerContext.next(maxExpires));
                } else {
                    emitter.complete(consumerContext.next());
                }
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

    }
}
