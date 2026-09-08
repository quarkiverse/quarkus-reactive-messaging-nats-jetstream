package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.serializer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.deployment.JetStreamBuildTimeConfiguration;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;

/**
 * Regression test for the codegen failure mode: when a forked codegen worker initializes
 * build-time configuration with a restricted thread context classloader (one that cannot see
 * extension jars), SmallRye Config's built-in ClassConverter resolves Class-typed properties
 * via the TCCL and fails. This test reproduces that exact scenario.
 */
class CodegenRegressionTest {

    private static final String DEFAULT_SERIALIZER = "io.quarkiverse.reactive.messaging.nats.jetstream.client.message.JacksonSerializer";

    private ClassLoader originalTccl;

    @BeforeEach
    void setup() {
        originalTccl = Thread.currentThread().getContextClassLoader();
    }

    @AfterEach
    void teardown() {
        Thread.currentThread().setContextClassLoader(originalTccl);
    }

    @Test
    void buildTimeConfigInitSucceedsWithRestrictedTccl() {
        URLClassLoader restrictedCl = new URLClassLoader(new URL[0], ClassLoader.getPlatformClassLoader());
        Thread.currentThread().setContextClassLoader(restrictedCl);

        SmallRyeConfig config = new SmallRyeConfigBuilder()
                .withMapping(JetStreamBuildTimeConfiguration.class)
                .build();
        JetStreamBuildTimeConfiguration mapping = config.getConfigMapping(JetStreamBuildTimeConfiguration.class);
        assertEquals(DEFAULT_SERIALIZER, mapping.serializer().toString());
    }
}
