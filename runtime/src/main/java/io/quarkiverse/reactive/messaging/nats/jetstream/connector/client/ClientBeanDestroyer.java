package io.quarkiverse.reactive.messaging.nats.jetstream.connector.client;

import java.util.Map;

import jakarta.enterprise.context.spi.CreationalContext;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkus.arc.BeanDestroyer;
import lombok.extern.jbosslog.JBossLog;

/**
 * Closes a JetStream {@link Client} synthetic CDI bean when its {@code @ApplicationScoped} context is destroyed,
 * i.e. on application shutdown. One {@link Client} bean is created per configured datasource by
 * {@code JetStreamProcessor} / {@code JetStreamRecorder}.
 */
@JBossLog
public class ClientBeanDestroyer implements BeanDestroyer<Client> {

    @Override
    public void destroy(Client instance, CreationalContext<Client> creationalContext, Map<String, Object> params) {
        try {
            instance.close();
        } catch (Exception e) {
            log.error("Could not close JetStream client: " + e.getMessage());
        }
    }
}
