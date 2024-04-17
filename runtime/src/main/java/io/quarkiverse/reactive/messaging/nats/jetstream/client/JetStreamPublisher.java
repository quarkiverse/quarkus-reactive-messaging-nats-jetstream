package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.quarkiverse.reactive.messaging.nats.jetstream.mapper.HeaderMapper.toJetStreamHeaders;
import static io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper.MESSAGE_TYPE_HEADER;
import static io.smallrye.reactive.messaging.tracing.TracingUtils.traceOutgoing;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.nats.client.JetStreamApiException;
import io.nats.client.PublishOptions;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamOutgoingMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamTrace;

@ApplicationScoped
public class JetStreamPublisher {
    private final PayloadMapper payloadMapper;
    private final Instrumenter<JetStreamTrace, Void> instrumenter;

    @Inject
    public JetStreamPublisher(PayloadMapper payloadMapper,
            final JetStreamInstrumenter jetStreamInstrumenter) {
        this.payloadMapper = payloadMapper;
        this.instrumenter = jetStreamInstrumenter.publisher();
    }

    public Message<?> publish(final Connection connection,
            final JetStreamPublishConfiguration configuration,
            final Message<?> message) {
        try {
            final var metadata = message.getMetadata(JetStreamOutgoingMessageMetadata.class);
            final var messageId = metadata.map(JetStreamOutgoingMessageMetadata::messageId)
                    .orElseGet(() -> UUID.randomUUID().toString());
            final var payload = payloadMapper.toByteArray(message.getPayload());

            final var headers = new HashMap<String, List<String>>();
            metadata.ifPresent(m -> headers.putAll(m.headers()));
            headers.putIfAbsent(MESSAGE_TYPE_HEADER, List.of(message.getPayload().getClass().getTypeName()));

            if (configuration.traceEnabled()) {
                // Create a new span for the outbound message and record updated tracing information in
                // the headers; this has to be done before we build the properties below
                traceOutgoing(instrumenter, message,
                        new JetStreamTrace(configuration.getStream(), configuration.getSubject(), messageId, headers,
                                new String(payload)));
            }

            final var jetStream = connection.jetStream();
            final var options = createPublishOptions(messageId, configuration.getStream());
            jetStream.publish(configuration.getSubject(), toJetStreamHeaders(headers), payload, options);

            return message;
        } catch (IOException | JetStreamApiException e) {
            throw new JetStreamPublishException(String.format("Failed to publish message: %s", e.getMessage()), e);
        }
    }

    private PublishOptions createPublishOptions(final String messageId, String streamName) {
        return PublishOptions.builder()
                .messageId(messageId)
                .stream(streamName)
                .build();
    }

}
