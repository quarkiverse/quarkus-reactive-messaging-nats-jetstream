package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.serializer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;

class InvalidSerializerConfigTest {

    @Test
    void configuredClassDoesNotImplementSerializer() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> loadAndValidate(String.class.getName()));
        assertTrue(ex.getMessage().contains("quarkus.messaging.nats.serializer"));
        assertTrue(ex.getMessage().contains(String.class.getName()));
        assertTrue(ex.getMessage().contains("io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer"));
    }

    @Test
    void configuredClassCannotBeFound() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> loadAndValidate("com.example.DoesNotExist"));
        assertTrue(ex.getMessage().contains("quarkus.messaging.nats.serializer"));
        assertTrue(ex.getMessage().contains("com.example.DoesNotExist"));
    }

    private Class<?> loadAndValidate(String fqcn) {
        Class<?> serializerClass;
        try {
            serializerClass = Class.forName(fqcn, true, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "quarkus.messaging.nats.serializer is set to '" + fqcn
                            + "', but this class could not be found on the application classpath.",
                    e);
        }
        if (!Serializer.class.isAssignableFrom(serializerClass)) {
            throw new RuntimeException(
                    "quarkus.messaging.nats.serializer is set to '" + fqcn
                            + "', but this class does not implement io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer.");
        }
        return serializerClass;
    }
}
