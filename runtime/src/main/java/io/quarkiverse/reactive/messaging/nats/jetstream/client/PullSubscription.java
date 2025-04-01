package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.nats.client.ConsumerContext;
import io.nats.client.JetStreamStatusException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerType;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

@RequiredArgsConstructor
@JBossLog
public class PullSubscription<T> implements Subscription<T> {
    private final PullConsumerConfiguration<T> consumerConfiguration;
    private final ConsumerContext consumerContext;
    private final MessageMapper messageMapper;
    private final TracerFactory tracerFactory;
    private final Context context;

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
        // nothing to do when using simplified NATS api
    }

    private Uni<Optional<io.nats.client.Message>> readNextMessage() {
        return Uni.createFrom().emitter(emitter -> {
            try {
                var maxExpires = consumerConfiguration.maxExpires();
                if (maxExpires != null) {
                    emitter.complete(Optional
                            .ofNullable(consumerContext.next(maxExpires)));
                } else {
                    emitter.complete(Optional
                            .ofNullable(consumerContext.next()));
                }
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
                    .item(() -> messageMapper.of(message, payloadType, context,
                            consumerConfiguration.consumerConfiguration().acknowledgeTimeout().orElse(null)));
        }
    }
}
