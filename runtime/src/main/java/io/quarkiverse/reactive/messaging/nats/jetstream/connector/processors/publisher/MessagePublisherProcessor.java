package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.publisher;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ChannelConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConsumerChannelConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.Health;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessageProcessor;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class MessagePublisherProcessor<T> implements MessageProcessor {
    private final ConsumerChannelConfiguration channelConfiguration;
    private final Client client;
    private final AtomicReference<Health> health;
    private volatile boolean stopped;

    public MessagePublisherProcessor(@NonNull final ConsumerChannelConfiguration channelConfiguration,
            @NonNull final Client client) {
        this.channelConfiguration = channelConfiguration;
        this.client = client;
        this.health = new AtomicReference<>(Health.builder().message("Publish processor inactive").healthy(false).build());
        this.stopped = false;
    }

    @Override
    public @NonNull ChannelConfiguration channelConfiguration() {
        return channelConfiguration;
    }

    @Override
    public @NonNull Health health() {
        return health.get();
    }

    @Override
    public void stop() {
        this.stopped = true;
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    public Multi<Message<T>> publisher() {
        return Multi.createFrom().deferred(() -> verifyConsumer().onItem().transformToMulti(consumer -> {
            health.set(new Health(true,
                    String.format("Publish processor healthy for channel: %s", channelConfiguration.name())));
            return subscribe();
        }))
                .onItem().invoke(() -> log.debugf("Received message from channel: %s", channelConfiguration.name()))
                .onFailure().invoke(failure -> {
                    log.errorf(failure, "An error occurred with message: %s", failure.getMessage());
                    health.set(new Health(false,
                            String.format("Publish processor unhealthy for channel: %s", channelConfiguration.name())));
                })
                .onFailure().retry().withBackOff(channelConfiguration.getRetryBackoff()).until(failure -> !stopped);

    }

    /**
     * Verifies that the consumer of the channel exists on the stream, so the channel is not reported ready before
     * it is able to receive messages.
     */
    private Uni<Consumer> verifyConsumer() {
        return client.consumerManagement(channelConfiguration.stream()).consumer(channelConfiguration.getConsumer())
                .onItem().ifNull().failWith(() -> new IllegalStateException(
                        String.format("Consumer %s not found on stream %s", channelConfiguration.getConsumer(),
                                channelConfiguration.stream())));
    }

    private Multi<Message<T>> subscribe() {
        return channelConfiguration.<T> payloadType()
                .map(payloadType -> client.subscribe(channelConfiguration.stream(), channelConfiguration.getConsumer(),
                        channelConfiguration.timeout(), channelConfiguration.batchSize(), payloadType))
                .orElseGet(() -> client.subscribe(channelConfiguration.stream(), channelConfiguration.getConsumer(),
                        channelConfiguration.timeout(), channelConfiguration.batchSize()));
    }
}
