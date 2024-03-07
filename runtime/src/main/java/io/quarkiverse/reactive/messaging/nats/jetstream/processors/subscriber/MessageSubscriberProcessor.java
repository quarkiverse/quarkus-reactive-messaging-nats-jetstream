package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import static io.quarkiverse.reactive.messaging.nats.jetstream.mapper.HeaderMapper.toJetStreamHeaders;
import static io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper.MESSAGE_TYPE_HEADER;
import static io.smallrye.reactive.messaging.tracing.TracingUtils.traceOutgoing;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.nats.client.PublishOptions;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamOutgoingMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamTrace;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.providers.helpers.MultiUtils;

public class MessageSubscriberProcessor implements MessageProcessor {
    private final static Logger logger = Logger.getLogger(MessageSubscriberProcessor.class);

    private final MessageSubscriberConfiguration configuration;
    private final JetStreamClient jetStreamClient;
    private final PayloadMapper payloadMapper;
    private final Instrumenter<JetStreamTrace, Void> instrumenter;
    private final String streamName;
    private final String subject;
    private final AtomicReference<Status> status;

    public MessageSubscriberProcessor(final JetStreamClient jetStreamClient,
            final MessageSubscriberConfiguration configuration,
            final PayloadMapper payloadMapper,
            final JetStreamInstrumenter jetStreamInstrumenter) {
        this.jetStreamClient = jetStreamClient;
        this.configuration = configuration;
        this.payloadMapper = payloadMapper;
        this.instrumenter = jetStreamInstrumenter.publisher();
        this.streamName = getStreamName(configuration);
        this.subject = getSubject(configuration);
        this.status = new AtomicReference<>(new Status(true, "Not connected"));
    }

    public Flow.Subscriber<? extends Message<?>> getSubscriber() {
        return MultiUtils.via(m -> m.onSubscription()
                .call(this::getOrEstablishConnection)
                .onItem()
                .transformToUniAndConcatenate(this::send)
                .onCompletion().invoke(this::close)
                .onTermination().invoke(this::close)
                .onFailure().invoke(throwable -> {
                    logger.errorf(throwable, "Failed to publish: %s", throwable.getMessage());
                    status.set(new Status(false, throwable.getMessage()));
                    close();
                }));
    }

    public Message<?> publish(final Connection connection, final Message<?> message) {
        try {
            final var metadata = message.getMetadata(JetStreamOutgoingMessageMetadata.class);
            final var messageId = metadata.map(JetStreamOutgoingMessageMetadata::messageId)
                    .orElseGet(() -> UUID.randomUUID().toString());
            final var payload = payloadMapper.toByteArray(message.getPayload());

            final var headers = new HashMap<String, List<String>>();
            metadata.ifPresent(m -> headers.putAll(m.headers()));
            headers.putIfAbsent(MESSAGE_TYPE_HEADER, List.of(message.getPayload().getClass().getTypeName()));

            if (configuration.traceEnabled()) {
                // Create a new span for the outbound message and record updated tracing information in
                // the headers; this has to be done before we build the properties below
                traceOutgoing(instrumenter, message,
                        new JetStreamTrace(streamName, subject, messageId, headers, new String(payload)));
            }

            final var jetStream = connection.jetStream();
            final var options = createPublishOptions(messageId, streamName);
            jetStream.publish(subject, toJetStreamHeaders(headers), payload, options);

            return message;
        } catch (IOException | JetStreamApiException e) {
            status.set(new Status(false, e.getMessage()));
            throw new MessageSubscriberException(String.format("Failed to publish message: %s", e.getMessage()), e);
        }
    }

    /**
     * Connections are made only on first message dispatch for subscribers. To avoid health is reporting not ok
     * the method returns true if connection is not established.
     */
    @Override
    public Status getStatus() {
        return status.get();
    }

    @Override
    public void close() {
        jetStreamClient.close();
    }

    @Override
    public String getChannel() {
        return configuration.getChannel();
    }

    private Uni<? extends Message<?>> send(Message<?> message) {
        return getOrEstablishConnection()
                .onItem()
                .transformToUni(connection -> send(message, connection));
    }

    private Uni<Message<?>> send(Message<?> message, Connection connection) {
        return Uni.createFrom().<Message<?>> emitter(em -> {
            try {
                em.complete(publish(connection, message));
            } catch (Throwable e) {
                logger.errorf(e, "Failed sending message: %s", e.getMessage());
                em.fail(e);
            }
        })
                .emitOn(runnable -> connection.context().runOnContext(runnable))
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(throwable -> notAcknowledge(message, throwable));
    }

    private Uni<Message<?>> acknowledge(Message<?> message) {
        return Uni.createFrom().completionStage(message.ack())
                .onItem().transform(v -> message);
    }

    private Uni<Message<?>> notAcknowledge(Message<?> message, Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(throwable))
                .onItem().transform(v -> null);
    }

    private Uni<Connection> getOrEstablishConnection() {
        return jetStreamClient.getOrEstablishConnection()
                .onItem().invoke(() -> status.set(new Status(true, "Is connected")))
                .onFailure().invoke(throwable -> status.set(
                        new Status(false, "Connection failed with message: " + throwable.getMessage())));
    }

    private String getStreamName(final MessageSubscriberConfiguration configuration) {
        return configuration.getStream()
                .orElseThrow(() -> new RuntimeException("Stream not configured for channel = " + configuration.getChannel()));
    }

    private String getSubject(final MessageSubscriberConfiguration configuration) {
        return configuration.getSubject()
                .orElseThrow(() -> new RuntimeException("Subject not configured for channel = " + configuration.getChannel()));
    }

    private PublishOptions createPublishOptions(final String messageId, String streamName) {
        return PublishOptions.builder()
                .messageId(messageId)
                .stream(streamName)
                .build();
    }
}
