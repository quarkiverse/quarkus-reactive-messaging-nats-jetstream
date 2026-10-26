package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

public interface External {

    /**
     * The subject prefix that imports the other account <code>$JS.API.CONSUMER.&gt; subjects</code>
     *
     * @return the api prefix
     */
    @NonNull
    Optional<String> api();

    /**
     * The delivery subject to use for the push consumer.
     *
     * @return delivery subject
     */
    @NonNull
    Optional<String> deliver();
}
