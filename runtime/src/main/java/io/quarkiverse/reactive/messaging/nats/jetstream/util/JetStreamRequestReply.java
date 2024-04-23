package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher.MessagePublisherConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.setup.JetStreamSetup;
import io.quarkiverse.reactive.messaging.nats.jetstream.setup.JetStreamSetupConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;

@ApplicationScoped
public class JetStreamRequestReply {
    private static final Logger logger = Logger.getLogger(JetStreamRequestReply.class);

    private final JetStreamClient jetStreamClient;
    private final JetStreamSetup jetStreamSetup;
    private final JetStreamPublisher jetStreamPublisher;
    private final MessageFactory messageFactory;

    @Inject
    public JetStreamRequestReply(NatsConfiguration natsConfiguration,
            ExecutionHolder executionHolder,
            MessageFactory messageFactory,
            JetStreamInstrumenter jetStreamInstrumenter,
            PayloadMapper payloadMapper) {
        this.jetStreamClient = new JetStreamClient(ConnectionConfiguration.of(natsConfiguration), executionHolder.vertx());
        this.jetStreamSetup = new JetStreamSetup();
        this.jetStreamPublisher = new JetStreamPublisher(payloadMapper, jetStreamInstrumenter);
        this.messageFactory = messageFactory;
    }

    public <T> Uni<Message<T>> request(Message<T> message, RequestReplyConfiguration<T> configuration) {
        return jetStreamClient.getOrEstablishConnection()
                .onItem()
                .transformToUni(connection -> request(connection, message, configuration));
    }

    public <T> Uni<Message<T>> nextReply(RequestReplyConfiguration<T> configuration) {
        return jetStreamClient.getOrEstablishConnection()
                .onItem()
                .transformToUni(connection -> nextReply(connection, configuration));
    }

    public void terminate(
            @Observes(notifyObserver = Reception.IF_EXISTS) @Priority(50) @BeforeDestroyed(ApplicationScoped.class) Object ignored) {
        try {
            jetStreamClient.close();
        } catch (Exception e) {
            logger.warnf(e, "Failed to close ");
        }
    }

    private <T> Uni<Message<T>> request(Connection connection, Message<T> message, RequestReplyConfiguration<T> configuration) {
        return Uni.createFrom().item(
                () -> jetStreamSetup.addOrUpdateStream(connection, JetStreamSetupConfiguration.of(configuration)))
                .emitOn(runnable -> connection.context().runOnContext(runnable))
                .onItem().transformToUni(streamResult -> Uni.createFrom()
                        .item(() -> jetStreamPublisher.publish(streamResult.connection(), new JetStreamPublishConfiguration() {
                            @Override
                            public boolean traceEnabled() {
                                return configuration.traceEnabled();
                            }

                            @Override
                            public String stream() {
                                return configuration.subject();
                            }

                            @Override
                            public String subject() {
                                return configuration.subject();
                            }
                        }, message)));
    }

    private <T> Uni<Message<T>> nextReply(Connection connection, RequestReplyConfiguration<T> configuration) {
        return Uni.createFrom().item(() -> {
            final JetStreamPullConsumerConfiguration pullConsumerConfiguration = JetStreamPullConsumerConfiguration
                    .of(configuration);
            try (JetStreamPuller puller = new JetStreamPuller(connection, pullConsumerConfiguration)) {
                return nextMessage(connection, puller, configuration);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).emitOn(runnable -> connection.context().runOnContext(runnable));
    }

    private <T> Message<T> nextMessage(Connection connection, JetStreamPuller puller,
            RequestReplyConfiguration<T> configuration) {
        return puller.nextMessage()
                .map(message -> messageFactory.create(message, configuration.traceEnabled(),
                        configuration.payloadType(), connection.context(),
                        MessagePublisherConfiguration.of(configuration)))
                .orElse(null);
    }
}
