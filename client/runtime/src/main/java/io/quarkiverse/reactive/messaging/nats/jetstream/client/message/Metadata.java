package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.*;

import org.jspecify.annotations.NonNull;

/**
 * An immutable collection of metadata objects attached to a {@link Message}, such as {@link PublishHeaders},
 * {@link MessageHeaders}, {@link MessageMetadata} or {@link AcknowledgeMetadata}. At most one item of each class is
 * kept: adding an item replaces any item of the same class.
 */
public final class Metadata implements Iterable<Object> {
    private static final Metadata EMPTY = new Metadata(List.of());

    private final List<Object> items;

    private Metadata(@NonNull List<Object> items) {
        this.items = Collections.unmodifiableList(items);
    }

    /**
     * @return a metadata collection without items
     */
    public static @NonNull Metadata empty() {
        return EMPTY;
    }

    /**
     * @param items the metadata items; must not be null
     * @return a metadata collection containing the given items
     */
    public static @NonNull Metadata of(@NonNull Object... items) {
        return from(List.of(items));
    }

    /**
     * @param items the metadata items; must not be null
     * @return a metadata collection containing the given items
     */
    public static @NonNull Metadata from(@NonNull Iterable<?> items) {
        var result = EMPTY;
        for (Object item : items) {
            result = result.with(item);
        }
        return result;
    }

    /**
     * @param item the item to add; must not be null
     * @return a copy of this collection containing the given item instead of any item of the same class
     */
    public @NonNull Metadata with(@NonNull Object item) {
        final var result = new ArrayList<>(items.size() + 1);
        for (Object existing : items) {
            if (existing.getClass() != item.getClass()) {
                result.add(existing);
            }
        }
        result.add(item);
        return new Metadata(result);
    }

    /**
     * @param type the class of the item to remove; must not be null
     * @return a copy of this collection without the items of the given class
     */
    public @NonNull Metadata without(@NonNull Class<?> type) {
        final var result = new ArrayList<>(items.size());
        for (Object existing : items) {
            if (existing.getClass() != type) {
                result.add(existing);
            }
        }
        return new Metadata(result);
    }

    /**
     * @param type the class of the item; must not be null
     * @return the first item that is an instance of the given class
     */
    public <M> @NonNull Optional<M> get(@NonNull Class<M> type) {
        for (Object item : items) {
            if (type.isInstance(item)) {
                return Optional.of(type.cast(item));
            }
        }
        return Optional.empty();
    }

    @Override
    public @NonNull Iterator<Object> iterator() {
        return items.iterator();
    }

    @Override
    public String toString() {
        return "Metadata" + items;
    }
}
