package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Represents the configuration required for connecting and interacting with a NATS JetStream container.
 * This configuration includes authentication credentials, SSL/TLS settings, and file paths for secure communication.
 */
public interface JetStreamContainerConfiguration {

    static JetStreamContainerConfiguration of(@NonNull String username, @NonNull String password, boolean sslEnabled,
            @Nullable String certificateFile,
            @Nullable String keyFile) {
        return new JetStreamContainerConfigurationImpl(
                username,
                password,
                sslEnabled,
                Optional.ofNullable(certificateFile),
                Optional.ofNullable(keyFile));
    }

    /**
     * Retrieves the username used for authenticating with the NATS JetStream container.
     *
     * @return the username as a String
     */
    @NonNull
    String username();

    /**
     * Retrieves the password used for authenticating with the NATS JetStream container.
     *
     * @return the password as a String
     */
    @NonNull
    String password();

    /**
     * Indicates whether SSL/TLS is enabled for the NATS JetStream container.
     *
     * @return true if SSL/TLS is enabled, false otherwise
     */
    boolean sslEnabled();

    /**
     * Retrieves the file path of the SSL/TLS certificate used for secure communication
     * with the NATS JetStream container.
     *
     * @return the file path of the certificate as a String
     */
    @NonNull
    Optional<String> certificateFile();

    /**
     * Retrieves the file path of the private key used for SSL/TLS authentication
     * with the NATS JetStream container.
     *
     * @return the file path of the private key as a String
     */
    @NonNull
    Optional<String> keyFile();

}
