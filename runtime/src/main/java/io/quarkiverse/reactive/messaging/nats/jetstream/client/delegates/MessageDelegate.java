package io.quarkiverse.reactive.messaging.nats.jetstream.client.delegates;

import static io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageFactory.MESSAGE_TYPE_HEADER;
import static io.smallrye.reactive.messaging.tracing.TracingUtils.traceOutgoing;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.*;
import io.nats.client.impl.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamOutgoingMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.administration.JetStreamSetupException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfigurtationFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.FetchConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PublishConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamTrace;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;

public class MessageDelegate {
    private final static Logger logger = Logger.getLogger(MessageConnection.class);

    public <T> Uni<Message<T>> publish(final io.nats.client.Connection connection,
            final MessageFactory messageFactory,
            final Context context,
            final JetStreamInstrumenter instrumenter,
            final Message<T> message,
            final PublishConfiguration configuration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var metadata = message.getMetadata(JetStreamOutgoingMessageMetadata.class);
                final var messageId = metadata.map(JetStreamOutgoingMessageMetadata::messageId)
                        .orElseGet(() -> UUID.randomUUID().toString());
                final var payload = messageFactory.toByteArray(message.getPayload());
                final var subject = metadata.flatMap(JetStreamOutgoingMessageMetadata::subtopic)
                        .map(subtopic -> configuration.subject() + "." + subtopic).orElseGet(configuration::subject);
                final var headers = new HashMap<String, List<String>>();
                metadata.ifPresent(m -> headers.putAll(m.headers()));
                if (message.getPayload() != null) {
                    headers.putIfAbsent(MESSAGE_TYPE_HEADER, List.of(message.getPayload().getClass().getTypeName()));
                }

                if (configuration.traceEnabled()) {
                    // Create a new span for the outbound message and record updated tracing information in
                    // the headers; this has to be done before we build the properties below
                    traceOutgoing(instrumenter.publisher(), message,
                            new JetStreamTrace(configuration.stream(), subject, messageId, headers,
                                    new String(payload)));
                }

                final var jetStream = connection.jetStream();
                final var options = createPublishOptions(messageId, configuration.stream());
                final var ack = jetStream.publish(
                        subject,
                        toJetStreamHeaders(headers),
                        payload,
                        options);

                if (logger.isDebugEnabled()) {
                    logger.debugf("Published message: %s", ack);
                }

                // flush all outgoing messages
                connection.flush(Duration.ZERO);

                return message;
            } catch (IOException | JetStreamApiException | JetStreamSetupException e) {
                throw new PublishException(String.format("Failed to publish message: %s", e.getMessage()), e);
            }
        }))
                .emitOn(context::runOnContext)
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(throwable -> notAcknowledge(message, throwable));
    }

    public <T> Uni<Message<T>> nextMessage(final io.nats.client.Connection connection,
            final Context context,
            final MessageFactory messageFactory,
            final FetchConsumerConfiguration<T> configuration) {
        return getConsumerContext(connection, context, configuration.stream(),
                configuration.name().orElseThrow(() -> new IllegalArgumentException("Consumer name is not configured")))
                .onItem()
                .transformToUni(consumerContext -> nextMessage(context, messageFactory, consumerContext, configuration));
    }

    public <T> Uni<Void> addOrUpdateConsumer(final io.nats.client.Connection connection,
            final Context context,
            final FetchConsumerConfiguration<T> configuration) {
        return Uni.createFrom().<Void> item(Unchecked.supplier(() -> {
            try {
                final var factory = new ConsumerConfigurtationFactory();
                final var consumerConfiguration = factory.create(configuration);
                final var streamContext = connection.getStreamContext(configuration.stream());
                streamContext.createOrUpdateConsumer(consumerConfiguration);
                connection.flush(Duration.ZERO);
                return null;
            } catch (IOException | JetStreamApiException e) {
                throw new FetchException(e);
            }
        }))
                .emitOn(context::runOnContext);
    }

    public <T> Uni<T> getKeyValue(final io.nats.client.Connection connection,
            final Context context,
            final MessageFactory messageFactory,
            final String bucketName,
            final String key,
            final Class<T> valueType) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                KeyValue keyValue = connection.keyValue(bucketName);
                return Optional.ofNullable(keyValue.get(key)).map(value -> messageFactory.decode(value.getValue(), valueType))
                        .orElse(null);
            } catch (IOException | JetStreamApiException e) {
                throw new KeyValueException(e);
            }
        }))
                .emitOn(context::runOnContext);
    }

    public <T> Uni<Void> putKeyValue(final io.nats.client.Connection connection,
            final Context context,
            final MessageFactory messageFactory,
            final String bucketName,
            final String key,
            final T value) {
        return Uni.createFrom().<Void> item(Unchecked.supplier(() -> {
            try {
                KeyValue keyValue = connection.keyValue(bucketName);
                keyValue.put(key, messageFactory.toByteArray(value));
                return null;
            } catch (IOException | JetStreamApiException e) {
                throw new KeyValueException(e);
            }
        }))
                .emitOn(context::runOnContext);
    }

    public Uni<Void> deleteKeyValue(final io.nats.client.Connection connection,
            final Context context,
            final String bucketName,
            final String key) {
        return Uni.createFrom().<Void> item(Unchecked.supplier(() -> {
            try {
                KeyValue keyValue = connection.keyValue(bucketName);
                keyValue.delete(key);
                return null;
            } catch (IOException | JetStreamApiException e) {
                throw new KeyValueException(e);
            }
        }))
                .emitOn(context::runOnContext);
    }

    public <T> Uni<Message<T>> resolve(final io.nats.client.Connection connection,
            final Context context,
            final MessageFactory messageFactory,
            final String streamName,
            final long sequence) {
        return Uni.createFrom().<Message<T>> emitter(emitter -> {
            try {
                final var jetStream = connection.jetStream();
                final var streamContext = jetStream.getStreamContext(streamName);
                final var messageInfo = streamContext.getMessage(sequence);
                emitter.complete(new JetStreamMessage<T>(messageInfo, messageFactory.<T> toPayload(messageInfo).orElse(null)));
            } catch (IOException | JetStreamApiException e) {
                emitter.fail(e);
            }
        })
                .emitOn(context::runOnContext);
    }

    private PublishOptions createPublishOptions(final String messageId, final String streamName) {
        return PublishOptions.builder()
                .messageId(messageId)
                .stream(streamName)
                .build();
    }

    private <T> Uni<Message<T>> acknowledge(final Message<T> message) {
        return Uni.createFrom().completionStage(message.ack())
                .onItem().transform(v -> message);
    }

    private <T> Uni<Message<T>> notAcknowledge(final Message<T> message, final Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(throwable))
                .onItem().invoke(() -> logger.warnf(throwable, "Message not published: %s", throwable.getMessage()))
                .onItem().transformToUni(v -> Uni.createFrom().item(message));
    }

    private Uni<ConsumerContext> getConsumerContext(final io.nats.client.Connection connection,
            final Context context,
            final String stream,
            final String consumerName) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var streamContext = connection.getStreamContext(stream);
                return streamContext.getConsumerContext(consumerName);
            } catch (IOException | JetStreamApiException e) {
                throw new FetchException(e);
            }
        }))
                .emitOn(context::runOnContext);
    }

    private Uni<io.nats.client.Message> nextMessage(final Context context, final ConsumerContext consumerContext,
            final Duration timeout) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                try (final var fetchConsumer = fetchConsumer(consumerContext, timeout)) {
                    final var message = fetchConsumer.nextMessage();
                    if (message != null) {
                        return message;
                    } else {
                        throw new MessageNotFoundException();
                    }
                }
            } catch (Throwable failure) {
                logger.errorf(failure, "Failed to fetch message: %s", failure.getMessage());
                throw new FetchException(failure);
            }
        }))
                .emitOn(context::runOnContext);
    }

    private FetchConsumer fetchConsumer(final ConsumerContext consumerContext, final Duration timeout)
            throws IOException, JetStreamApiException {
        if (timeout == null) {
            return consumerContext.fetch(FetchConsumeOptions.builder().maxMessages(1).noWait().build());
        } else {
            return consumerContext.fetch(FetchConsumeOptions.builder().maxMessages(1).expiresIn(timeout.toMillis()).build());
        }
    }

    private <T> Uni<Message<T>> nextMessage(final Context context,
            final MessageFactory messageFactory,
            final ConsumerContext consumerContext,
            final FetchConsumerConfiguration<T> configuration) {
        return nextMessage(context, consumerContext, configuration.fetchTimeout().orElse(null))
                .map(message -> messageFactory.create(
                        message,
                        configuration.traceEnabled(),
                        configuration.payloadType().orElse(null),
                        context,
                        new ExponentialBackoff(false, Duration.ZERO),
                        configuration.ackTimeout()));
    }

    private Headers toJetStreamHeaders(Map<String, List<String>> headers) {
        final var result = new Headers();
        headers.forEach(result::add);
        return result;
    }

}
