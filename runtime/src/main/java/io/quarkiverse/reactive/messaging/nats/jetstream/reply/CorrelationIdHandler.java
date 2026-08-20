package io.quarkiverse.reactive.messaging.nats.jetstream.reply;

import java.util.Optional;

/**
 * Strategy for generating and parsing correlation ids that pair requests with replies. Provide an alternate implementation
 * as a CDI bean (for example one backed by OpenTelemetry trace context) and select it per channel with the
 * {@code reply.correlation-id.handler} connector attribute. The default is {@link UuidCorrelationIdHandler}.
 */
public interface CorrelationIdHandler {

    /** @return a new correlation id that will be placed in the {@link JetStreamRequestReply#CORRELATION_ID_HEADER} header */
    String generate();

    /**
     * Parses a correlation id carried by an incoming reply.
     *
     * @param value the raw header value
     * @return the parsed id, or empty if it is malformed and the message should be ignored
     */
    Optional<String> parse(String value);
}
