package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.nats.client.Connection.Status.CONNECTED;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.jboss.logging.Logger;

import io.nats.client.*;
import io.vertx.mutiny.core.Context;

public class Connection implements AutoCloseable {
    private final static Logger logger = Logger.getLogger(Connection.class);

    private final io.nats.client.Connection connection;
    private final Context context;

    public Connection(final io.nats.client.Connection connection, final Context context) {
        this.connection = connection;
        this.context = context;
    }

    public io.nats.client.Connection connection() {
        return connection;
    }

    public Context context() {
        return context;
    }

    public JetStream jetStream() throws IOException {
        return connection.jetStream();
    }

    public JetStreamManagement jetStreamManagement() throws IOException {
        return connection.jetStreamManagement();
    }

    public Dispatcher createDispatcher() {
        return connection.createDispatcher();
    }

    public io.nats.client.Connection.Status getStatus() {
        return connection.getStatus();
    }

    public boolean isConnected() {
        return CONNECTED.equals(getStatus());
    }

    public StreamContext getStreamContext(String stream) throws IOException, JetStreamApiException {
        return connection.getStreamContext(stream);
    }

    public void flush(Duration duration) {
        try {
            connection.flush(duration);
        } catch (TimeoutException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        }
    }
}
