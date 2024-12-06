package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.nats.client.Options.DEFAULT_MAX_RECONNECT;
import static io.nats.client.Options.DEFAULT_RECONNECT_WAIT;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.ConsumerMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.StreamStateMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

@ApplicationScoped
public class DefaultConnectionFactory implements ConnectionFactory {
    private final static Logger logger = Logger.getLogger(DefaultConnectionFactory.class);

    private final MessageMapper messageMapper;
    private final PayloadMapper payloadMapper;
    private final ConsumerMapper consumerMapper;
    private final StreamStateMapper streamStateMapper;

    @Inject
    public DefaultConnectionFactory(final MessageMapper messageMapper,
            final PayloadMapper payloadMapper,
            final ConsumerMapper consumerMapper,
            final StreamStateMapper streamStateMapper) {
        this.messageMapper = messageMapper;
        this.payloadMapper = payloadMapper;
        this.consumerMapper = consumerMapper;
        this.streamStateMapper = streamStateMapper;
    }

    @Override
    public Uni<? extends Connection> create(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener) {
        return Uni.createFrom().item(Unchecked.supplier(() -> new DefaultConnection(connectionConfiguration,
                connectionListener, messageMapper, payloadMapper, consumerMapper, streamStateMapper)))
                .onFailure().invoke(failure -> logger.errorf(failure, "Failed connecting to NATS: %s", failure.getMessage()))
                .onFailure()
                .retry()
                .withBackOff(connectionConfiguration.connectionBackoff().orElse(DEFAULT_RECONNECT_WAIT))
                .atMost(connectionConfiguration.connectionAttempts().orElse(DEFAULT_MAX_RECONNECT));
    }

}
