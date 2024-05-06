package io.quarkiverse.reactive.messaging.nats.jetstream.administration;

import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;

@ApplicationScoped
public class MessageResolver {
    private static final Logger logger = Logger.getLogger(MessageResolver.class);
    private final NatsConfiguration configuration;
    private final PayloadMapper payloadMapper;
    private final ExecutionHolder executionHolder;

    @Inject
    public MessageResolver(NatsConfiguration configuration, PayloadMapper payloadMapper, ExecutionHolder executionHolder) {
        this.configuration = configuration;
        this.payloadMapper = payloadMapper;
        this.executionHolder = executionHolder;
    }

    public <T> Uni<Message<T>> resolve(String streamName, long sequence) {
        final var client = new JetStreamClient(ConnectionConfiguration.of(configuration), executionHolder.vertx());
        return client.getOrEstablishConnection()
                .onItem().<Message<T>> transformToUni(connection -> resolve(connection, streamName, sequence))
                .onItem().invoke(m -> client.close())
                .onFailure().invoke(throwable -> {
                    logger.errorf(throwable, "Failed to resolve message: %s", throwable.getMessage());
                    client.close();
                });
    }

    private <T> Uni<Message<T>> resolve(Connection connection, String streamName, long sequence) {
        return Uni.createFrom().emitter(emitter -> {
            try {
                final var jetStream = connection.jetStream();
                final var streamContext = jetStream.getStreamContext(streamName);
                final var messageInfo = streamContext.getMessage(sequence);
                emitter.complete(new JetStreamMessage<T>(messageInfo, payloadMapper.<T> toPayload(messageInfo).orElse(null)));
            } catch (IOException | JetStreamApiException e) {
                emitter.fail(e);
            }
        });
    }
}
