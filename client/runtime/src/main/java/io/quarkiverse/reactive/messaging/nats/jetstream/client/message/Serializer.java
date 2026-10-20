package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

/**
 * This interface defines the contract for serializing and deserializing objects.
 * Implementations of this interface provide mechanisms for converting objects
 * to and from their byte array representations.
 */
public interface Serializer {

    /**
     * Deserializes the given byte array into an object of the specified type.
     *
     * @param data the byte array containing the serialized object data
     * @param type the class type to which the data should be deserialized
     * @param <T> the type of the object to be deserialized
     * @return an instance of the specified type containing the deserialized data
     */
    <T> T readValue(byte[] data, Class<T> type);

    /**
     * Converts the given payload into a byte array representation.
     *
     * @param payload the object to be serialized into a byte array
     * @param <T> the type of the object being serialized
     * @return a byte array representing the serialized form of the payload
     */
    <T> byte[] toBytes(T payload);
}
