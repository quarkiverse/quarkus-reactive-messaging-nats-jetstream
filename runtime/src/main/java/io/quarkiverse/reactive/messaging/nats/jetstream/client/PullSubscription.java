package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessage.DEFAULT_ACK_TIMEOUT;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerType;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.Context;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
class PullSubscription<T> implements Subscription<T> {
    private final TracerFactory tracerFactory;
    private final Context context;
    private final PullMessage pullMessage;
    private final MessageMapper messageMapper;
    private final PullConsumerConfiguration consumerConfiguration;

    public PullSubscription(
            final PullMessage pullMessage,
            final PullConsumerConfiguration consumerConfiguration,
            final MessageMapper messageMapper,
            final TracerFactory tracerFactory,
            final Context context) {
        this.pullMessage = pullMessage;
        this.tracerFactory = tracerFactory;
        this.context = context;
        this.messageMapper = messageMapper;
        this.consumerConfiguration = consumerConfiguration;
    }

    @SuppressWarnings({ "ReactiveStreamsUnusedPublisher", "unchecked" })
    @Override
    public Multi<Message<T>> subscribe() {
        Class<T> payloadType = (Class<T>) consumerConfiguration.consumerConfiguration().payloadType().orElse(null);
        final var tracer = tracerFactory.<T> create(TracerType.Subscribe);
        ExecutorService pullExecutor = Executors.newSingleThreadExecutor(JetstreamWorkerThread::new);
        return Multi.createBy().repeating()
                .uni(pullMessage::next)
                .whilst(message -> true)
                .runSubscriptionOn(pullExecutor)
                .emitOn(context::runOnContext)
                .flatMap(message -> createMulti(message, payloadType, context))
                .onItem().transformToUniAndMerge(message -> tracer.withTrace(message, msg -> msg));
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

    @Override
    public void close() {
        pullMessage.close();
    }
}
