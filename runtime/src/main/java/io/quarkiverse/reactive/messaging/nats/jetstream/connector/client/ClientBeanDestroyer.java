package io.quarkiverse.reactive.messaging.nats.jetstream.connector.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkus.arc.BeanDestroyer;
import io.quarkus.arc.SyntheticCreationalContext;
import lombok.extern.jbosslog.JBossLog;

/**
 * Closes a JetStream {@link Client} synthetic CDI bean when its {@code @ApplicationScoped} context is destroyed,
 * i.e. on application shutdown. One {@link Client} bean is created per configured datasource by
 * {@code JetStreamProcessor} and created by {@link ClientBeanCreator}.
 */
@JBossLog
public class ClientBeanDestroyer implements BeanDestroyer<Client> {

    @Override
    public void destroy(Client instance, SyntheticCreationalContext<Client> context) {
        try {
            instance.close();
        } catch (Exception e) {
            log.error("Could not close JetStream client: " + e.getMessage());
        }
    }
}
