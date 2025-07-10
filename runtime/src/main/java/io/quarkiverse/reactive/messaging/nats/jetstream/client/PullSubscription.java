package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessage.DEFAULT_ACK_TIMEOUT;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.nats.client.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerType;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class PullSubscription<T> extends AbstractConsumer implements Subscription<T> {
    private final String stream;
    private final String consumer;
    private final PullConsumerConfiguration consumerConfiguration;
    private final JetStreamReader reader;
    private final JetStreamSubscription subscription;
    private final MessageMapper messageMapper;
    private final TracerFactory tracerFactory;
    private final Context context;

    public PullSubscription(final String stream,
            final String consumer,
            final PullConsumerConfiguration consumerConfiguration,
            final MessageMapper messageMapper,
            final TracerFactory tracerFactory,
            final JetStream jetStream,
            final Context context) throws IOException, JetStreamApiException {
        this.stream = stream;
        this.consumer = consumer;
        this.consumerConfiguration = consumerConfiguration;

        PullSubscribeOptions options = createOptions(consumerConfiguration);
        this.subscription = jetStream.subscribe(null, options);
        this.reader = subscription.reader(consumerConfiguration.pullConfiguration().batchSize(),
                consumerConfiguration.pullConfiguration().rePullAt());

        this.messageMapper = messageMapper;
        this.tracerFactory = tracerFactory;
        this.context = context;
    }

    @SuppressWarnings({ "ReactiveStreamsUnusedPublisher", "unchecked" })
    @Override
    public Multi<Message<T>> subscribe() {
        Class<T> payloadType = (Class<T>) consumerConfiguration.consumerConfiguration().payloadType().orElse(null);
        final var tracer = tracerFactory.<T> create(TracerType.Subscribe);
        ExecutorService pullExecutor = Executors.newSingleThreadExecutor(JetstreamWorkerThread::new);
        return Multi.createBy().repeating()
                .uni(this::readNextMessage)
                .whilst(message -> true)
                .runSubscriptionOn(pullExecutor)
                .emitOn(context::runOnContext)
                .flatMap(message -> createMulti(message.orElse(null), payloadType, context))
                .onItem().transformToUniAndMerge(message -> tracer.withTrace(message, msg -> msg));
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

    private Uni<Optional<io.nats.client.Message>> readNextMessage() {
        return Uni.createFrom().emitter(emitter -> {
            try {
                emitter.complete(Optional
                        .ofNullable(reader.nextMessage(consumerConfiguration.pullConfiguration().maxExpires())));
            } catch (JetStreamStatusException e) {
                emitter.fail(new PullException(e));
            } catch (IllegalStateException e) {
                emitter.complete(Optional.empty());
            } catch (InterruptedException e) {
                emitter.fail(new PullException(String.format("The reader was interrupted for stream: %s", stream), e));
            } catch (Exception exception) {
                emitter.fail(new PullException(String.format("Error reading next message from stream: %s", stream), exception));
            }
        });
    }

    private Multi<Message<T>> createMulti(io.nats.client.Message message,
            Class<T> payloadType, Context context) {
        if (message == null || message.getData() == null) {
            return Multi.createFrom().empty();
        } else {
            return Multi.createFrom()
                    .item(() -> messageMapper.of(message, payloadType, context,
                            consumerConfiguration.consumerConfiguration().acknowledgeTimeout().orElse(DEFAULT_ACK_TIMEOUT),
                            consumerConfiguration.consumerConfiguration().backoff().orElseGet(List::of)));
        }
    }

    private PullSubscribeOptions createOptions(PullConsumerConfiguration consumerConfiguration) {
        var builder = PullSubscribeOptions.builder();
        builder = builder.stream(stream);
        builder = builder.configuration(createConsumerConfiguration(consumer, consumerConfiguration));
        return builder.build();
    }
}
