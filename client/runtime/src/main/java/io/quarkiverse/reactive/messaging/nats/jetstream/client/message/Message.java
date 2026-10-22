package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Context;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;

/**
 * A message published to or received from NATS JetStream: a payload together with its {@link Metadata} and the
 * functions acknowledging it. Messages are immutable; the {@code with...} methods return modified copies.
 * <p>
 * A received message carries its {@link MessageHeaders} and {@link MessageMetadata}, and acknowledging it
 * acknowledges it on the server. A message to be published may carry {@link PublishHeaders}; the published message
 * additionally carries the {@link AcknowledgeMetadata} returned by the server.
 *
 * @param <T> the type of the payload
 */
public interface Message<T> {

    /**
     * @param payload the payload; may be null
     * @return a message with the given payload, no metadata and no-op acknowledgements
     */
    static <T> @NonNull Message<T> of(@Nullable T payload) {
        return of(payload, Metadata.empty());
    }

    /**
     * @param payload the payload; may be null
     * @param metadata the metadata; must not be null
     * @return a message with the given payload and metadata and no-op acknowledgements
     */
    static <T> @NonNull Message<T> of(@Nullable T payload, @NonNull Metadata metadata) {
        return of(payload, metadata, MessageImpl.NO_ACK, MessageImpl.NO_NACK);
    }

    /**
     * @param payload the payload; may be null
     * @param metadata the metadata; must not be null
     * @param ack acknowledges the message with the given acknowledgement metadata; must not be null
     * @param nack negatively acknowledges the message with the given reason and acknowledgement metadata; must not be
     *        null
     * @return a message with the given payload, metadata and acknowledgements
     */
    static <T> @NonNull Message<T> of(@Nullable T payload, @NonNull Metadata metadata,
            @NonNull Function<Metadata, CompletionStage<Void>> ack,
            @NonNull BiFunction<Throwable, Metadata, CompletionStage<Void>> nack) {
        return new MessageImpl<>(payload, metadata, ack, nack);
    }

    /**
     * Creates a message received from JetStream. It carries the {@link MessageMetadata}, the {@link MessageHeaders}
     * and the {@link ConsumerConfiguration} of the consumer it was received by, and acknowledging it acknowledges it
     * on the server.
     *
     * @param message the received message; must not be null
     * @param context the context the acknowledgements run on; must not be null
     * @param consumerConfiguration the configuration of the consumer the message was received by; must not be null
     * @return the received message
     */
    static @NonNull Message<byte[]> of(@NonNull NativeMessage message,
            @NonNull Context context,
            @NonNull ConsumerConfiguration consumerConfiguration) {
        return MessageImpl.received(message, context, consumerConfiguration);
    }

    /**
     * @return the payload; may be null
     */
    @Nullable
    T getPayload();

    /**
     * @return the metadata of the message
     */
    @NonNull
    Metadata getMetadata();

    /**
     * @param type the class of the metadata item; must not be null
     * @return the first metadata item that is an instance of the given class
     */
    default <M> @NonNull Optional<M> getMetadata(@NonNull Class<M> type) {
        return getMetadata().get(type);
    }

    /**
     * @return the headers of the message: its {@link PublishHeaders} and {@link MessageHeaders}
     */
    default @NonNull List<? extends Headers> getHeaders() {
        final var headers = new ArrayList<Headers>();
        for (final var item : getMetadata()) {
            if (item instanceof Headers header) {
                headers.add(header);
            }
        }
        return headers;
    }

    /**
     * @return the function acknowledging the message with acknowledgement metadata
     */
    @NonNull
    Function<Metadata, CompletionStage<Void>> getAckWithMetadata();

    /**
     * @return the function negatively acknowledging the message with a reason and acknowledgement metadata
     */
    @NonNull
    BiFunction<Throwable, Metadata, CompletionStage<Void>> getNackWithMetadata();

    /**
     * Acknowledges the message.
     *
     * @return a stage completed when the message is acknowledged
     */
    default @NonNull CompletionStage<Void> ack() {
        return ack(Metadata.empty());
    }

    /**
     * Acknowledges the message.
     *
     * @param metadata the acknowledgement metadata; its {@link ContextMetadata} is the context the returned stage
     *        completes on
     * @return a stage completed when the message is acknowledged
     */
    default @NonNull CompletionStage<Void> ack(@NonNull Metadata metadata) {
        return getAckWithMetadata().apply(metadata);
    }

    /**
     * Negatively acknowledges the message.
     *
     * @param reason the reason; must not be null
     * @return a stage completed when the message is negatively acknowledged
     */
    default @NonNull CompletionStage<Void> nack(@NonNull Throwable reason) {
        return nack(reason, Metadata.empty());
    }

    /**
     * Negatively acknowledges the message. A received message is redelivered after the delay of the
     * {@link NotAcknowledgeMetadata} of the acknowledgement metadata, if any.
     *
     * @param reason the reason; must not be null
     * @param metadata the acknowledgement metadata; its {@link ContextMetadata} is the context the returned stage
     *        completes on
     * @return a stage completed when the message is negatively acknowledged
     */
    default @NonNull CompletionStage<Void> nack(@NonNull Throwable reason, @NonNull Metadata metadata) {
        return getNackWithMetadata().apply(reason, metadata);
    }

    /**
     * @param payload the new payload; may be null
     * @return a copy of this message with the given payload
     */
    default <R> @NonNull Message<R> withPayload(@Nullable R payload) {
        return of(payload, getMetadata(), getAckWithMetadata(), getNackWithMetadata());
    }

    /**
     * @param metadata the new metadata; must not be null
     * @return a copy of this message with the given metadata
     */
    default @NonNull Message<T> withMetadata(@NonNull Metadata metadata) {
        return of(getPayload(), metadata, getAckWithMetadata(), getNackWithMetadata());
    }

    /**
     * @param item the metadata item to add; must not be null
     * @return a copy of this message with the given item instead of any item of the same class
     */
    default @NonNull Message<T> addMetadata(@NonNull Object item) {
        return withMetadata(getMetadata().with(item));
    }

    /**
     * @param ack the new acknowledgement; must not be null
     * @return a copy of this message with the given acknowledgement
     */
    default @NonNull Message<T> withAckWithMetadata(@NonNull Function<Metadata, CompletionStage<Void>> ack) {
        return of(getPayload(), getMetadata(), ack, getNackWithMetadata());
    }

    /**
     * @param nack the new negative acknowledgement; must not be null
     * @return a copy of this message with the given negative acknowledgement
     */
    default @NonNull Message<T> withNackWithMetadata(
            @NonNull BiFunction<Throwable, Metadata, CompletionStage<Void>> nack) {
        return of(getPayload(), getMetadata(), getAckWithMetadata(), nack);
    }

    /**
     * @return a completed stage, for messages without acknowledgement
     */
    static @NonNull CompletionStage<Void> completed() {
        return CompletableFuture.completedFuture(null);
    }
}
