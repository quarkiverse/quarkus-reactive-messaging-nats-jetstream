package io.quarkiverse.reactive.nats.message;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public interface Headers {

    static Headers of() {
        return new HeadersDelegate(new io.nats.client.impl.Headers());
    }

    static Headers of(io.nats.client.impl.Headers headers) {
        return new HeadersDelegate(headers);
    }

    /**
     * If the key is present add the values to the list of values for the key.
     * If the key is not present, sets the specified values for the key.
     * null values are ignored. If all values are null, the key is not added or updated.
     *
     * @param key    the key
     * @param values the values
     * @return the Headers object
     * @throws IllegalArgumentException if the key is null or empty or contains invalid characters
     *                                  -or- if any value contains invalid characters
     */
    Headers add(String key, String... values);

    Headers add(String key, Collection<String> values);

    /**
     * Associates the specified values with the key. If the key was already present
     * any existing values are removed and replaced with the new list.
     * null values are ignored. If all values are null, the put is ignored
     *
     * @param key    the key
     * @param values the values
     * @return the Headers object
     * @throws IllegalArgumentException if the key is null or empty or contains invalid characters
     *                                  -or- if any value contains invalid characters
     */
    Headers put(String key, String... values);

    /**
     * Associates the specified values with the key. If the key was already present
     * any existing values are removed and replaced with the new list.
     * null values are ignored. If all values are null, the put is ignored
     *
     * @param key    the key
     * @param values the values
     * @return the Headers object
     * @throws IllegalArgumentException if the key is null or empty or contains invalid characters
     *                                  -or- if any value contains invalid characters
     */
    Headers put(String key, Collection<String> values);

    /**
     * Associates all specified values with their key. If the key was already present
     * any existing values are removed and replaced with the new list.
     * null values are ignored. If all values are null, the put is ignored
     *
     * @param map the map
     * @return the Headers object
     */
    Headers put(Map<String, List<String>> map);

    /**
     * Removes each key and its values if the key was present
     *
     * @param keys the key or keys to remove
     */
    void remove(String... keys);

    /**
     * Removes each key and its values if the key was present
     *
     * @param keys the key or keys to remove
     */
    void remove(Collection<String> keys);


    /**
     * Returns the number of keys (case-sensitive) in the header.
     *
     * @return the number of header entries
     */
    int size();

    /**
     * Returns ture if map contains no keys.
     *
     * @return true if there are no headers
     */
    boolean isEmpty();

    /**
     * Removes all the keys The object map will be empty after this call returns.
     */
    void clear();

    /**
     * Returns true if key (case-sensitive) is present (has values)
     *
     * @param key key whose presence is to be tested
     * @return true if the key (case-sensitive) is present (has values)
     */
    boolean containsKey(String key);

    /**
     * Returns true if key (case-insensitive) is present (has values)
     *
     * @param key exact key whose presence is to be tested
     * @return true if the key (case-insensitive) is present (has values)
     */
    boolean containsKeyIgnoreCase(String key);

    /**
     * Returns a {@link Set} view of the keys (case-sensitive) contained in the object.
     *
     * @return a read-only set the keys contained in this map
     */
    Set<String> keySet();

    /**
     * Returns a {@link Set} view of the keys (case-insensitive) contained in the object.
     *
     * @return a read-only set of keys (in lowercase) contained in this map
     */
    Set<String> keySetIgnoreCase();

    /**
     * Returns a {@link List} view of the values for the specific (case-sensitive) key.
     * Will be {@code null} if the key is not found.
     *
     * @param key the key whose associated value is to be returned
     * @return a read-only list of the values for the case-sensitive key.
     */
    @Nullable
    List<String> get(String key);

    /**
     * Returns the first value for the specific (case-sensitive) key.
     * Will be {@code null} if the key is not found.
     *
     * @param key the key whose associated value is to be returned
     * @return the first value for the case-sensitive key.
     */
    @Nullable
    String getFirst(String key);

    /**
     * Returns the last value for the specific (case-sensitive) key.
     * Will be {@code null} if the key is not found.
     *
     * @param key the key whose associated value is to be returned
     * @return the last value for the case-sensitive key.
     */
    @Nullable
    String getLast(String key);

    /**
     * Returns a {@link List} view of the values for the specific (case-insensitive) key.
     * Will be {@code null} if the key is not found.
     *
     * @param key the key whose associated value is to be returned
     * @return a read-only list of the values for the case-insensitive key.
     */
    @Nullable
    List<String> getIgnoreCase(String key);

    /**
     * Returns a {@link Set} read only view of the mappings contained in the header (case-sensitive keys).
     * The set is not modifiable and any attempt to modify will throw an exception.
     *
     * @return a set view of the mappings contained in this map or Collections.emptySet() if there are no entries
     */
    @NonNull
    Set<Map.Entry<String, List<String>>> entrySet();

    /**
     * Returns if the headers are dirty, which means the serialization
     * has not been done so also don't know the byte length
     *
     * @return true if dirty
     */
    boolean isDirty();

    /**
     * Whether the entire Headers is read only
     *
     * @return the read only state
     */
    boolean isReadOnly();
}
