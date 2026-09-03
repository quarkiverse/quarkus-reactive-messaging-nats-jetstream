package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.reply;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.mutiny.Uni;

/**
 * Replier: consumes requests on 'orders' and echoes them, preserving the incoming message metadata so that the
 * requestor's advertised reply subject and correlation id drive auto-routing.
 */
@ApplicationScoped
public class ReplierBean {

    @Incoming("replies-in")
    @Outgoing("replies-out")
    public Uni<Message<String>> reply(Message<String> msg) {
        return Uni.createFrom().completionStage(msg.ack())
                .chain(() -> Uni.createFrom().item(
                        Message.of("echo:" + msg.getPayload()).withMetadata(msg.getMetadata())));
    }

}
