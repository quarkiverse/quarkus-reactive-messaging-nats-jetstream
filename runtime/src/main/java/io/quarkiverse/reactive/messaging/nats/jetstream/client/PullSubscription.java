package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.nats.client.JetStreamReader;
import io.nats.client.JetStreamStatusException;
import io.nats.client.JetStreamSubscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerType;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class PullSubscription<T> implements Subscription<T> {
    private final PullConsumerConfiguration<T> consumerConfiguration;
    private final JetStreamReader reader;
    private final JetStreamSubscription subscription;
    private final MessageMapper messageMapper;
    private final TracerFactory tracerFactory;
    private final Context context;

    PullSubscription(PullConsumerConfiguration<T> consumerConfiguration,
            JetStreamSubscription subscription,
            JetStreamReader reader,
            MessageMapper messageMapper,
            TracerFactory tracerFactory,
            Context context) {
        this.consumerConfiguration = consumerConfiguration;
        this.subscription = subscription;
        this.reader = reader;
        this.messageMapper = messageMapper;
        this.tracerFactory = tracerFactory;
        this.context = context;
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public Multi<Message<T>> subscribe() {
        Class<T> payloadType = consumerConfiguration.consumerConfiguration().payloadType().orElse(null);
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
        try {
            if (subscription.isActive()) {
                subscription.unsubscribe();
            }
        } catch (Exception e) {
            log.warnf("Failed to unsubscribe subscription with message %s", e.getMessage());
        }
    }

    private Uni<Optional<io.nats.client.Message>> readNextMessage() {
        return Uni.createFrom().emitter(emitter -> {
            try {
                emitter.complete(Optional
                        .ofNullable(reader.nextMessage(consumerConfiguration.maxExpires())));
            } catch (JetStreamStatusException e) {
                emitter.fail(new PullException(e));
            } catch (IllegalStateException e) {
                emitter.complete(Optional.empty());
            } catch (InterruptedException e) {
                emitter.fail(new PullException(String.format("The reader was interrupted for stream: %s",
                        consumerConfiguration.consumerConfiguration().stream()), e));
            } catch (Exception exception) {
                emitter.fail(new PullException(String.format("Error reading next message from stream: %s",
                        consumerConfiguration.consumerConfiguration().stream()), exception));
            }
        });
    }

    private Multi<Message<T>> createMulti(io.nats.client.Message message,
            Class<T> payloadType, Context context) {
        if (message == null || message.getData() == null) {
            return Multi.createFrom().empty();
        } else {
            return Multi.createFrom()
                    .item(() -> messageMapper.of(message, payloadType, context));
        }
    }
}
