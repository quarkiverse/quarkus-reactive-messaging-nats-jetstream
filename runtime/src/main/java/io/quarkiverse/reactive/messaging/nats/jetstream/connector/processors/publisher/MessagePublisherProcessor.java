package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.publisher;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ChannelConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConsumerChannelConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.Health;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessageProcessor;
import io.smallrye.mutiny.Multi;
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

    public Multi<Message<T>> publisher() {
        return subscribe()
                .onItem().invoke(() -> log.debugf("Received message from channel: %s", channelConfiguration.name()))
                .onSubscription()
                .invoke(() -> health
                        .set(new Health(true,
                                String.format("Publish processor healthy for channel: %s", channelConfiguration.name()))))
                .onFailure().invoke(failure -> {
                    log.errorf(failure, "An error occurred with message: %s", failure.getMessage());
                    health.set(new Health(false,
                            String.format("Publish processor unhealthy for channel: %s", channelConfiguration.name())));
                })
                .onFailure().retry().withBackOff(channelConfiguration.getRetryBackoff()).until(failure -> stopped);

    }

    private Multi<Message<T>> subscribe() {
        return channelConfiguration.<T> payloadType()
                .map(payloadType -> client.subscribe(channelConfiguration.stream(), channelConfiguration.getConsumer(),
                        channelConfiguration.timeout(), channelConfiguration.batchSize(), payloadType))
                .orElseGet(() -> client.subscribe(channelConfiguration.stream(), channelConfiguration.getConsumer(),
                        channelConfiguration.timeout(), channelConfiguration.batchSize()));
    }
}
