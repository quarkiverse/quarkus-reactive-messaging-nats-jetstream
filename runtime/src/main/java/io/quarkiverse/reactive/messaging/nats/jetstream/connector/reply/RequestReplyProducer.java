package io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;

import io.smallrye.reactive.messaging.ChannelRegistry;
import io.smallrye.reactive.messaging.providers.extension.ChannelProducer;

/**
 * Resolves {@code @Channel} injections of {@link RequestReply}. The channel name is read from the injection
 * point at runtime (the {@link Channel#value()} member is non-binding), mirroring how SmallRye resolves standard
 * emitters.
 */
@ApplicationScoped
public class RequestReplyProducer {

    @Inject
    ChannelRegistry channelRegistry;

    @SuppressWarnings("unchecked")
    @Produces
    @Typed(RequestReply.class)
    @Channel("") // Stream name is ignored during type-safe resolution
    <Req, Rep> RequestReply<Req, Rep> produce(InjectionPoint injectionPoint) {
        String channelName = ChannelProducer.getChannelName(injectionPoint);
        RequestReply<Req, Rep> emitter = (RequestReply<Req, Rep>) channelRegistry
                .getEmitter(channelName, RequestReply.class);
        if (emitter == null) {
            throw new IllegalStateException(
                    "No JetStream request-reply emitter registered for channel '" + channelName
                            + "'. Ensure the channel is configured with connector 'quarkus-jetstream' and a reply subject.");
        }
        return emitter;
    }

}
