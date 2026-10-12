package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors;

import io.quarkus.arc.BeanDestroyer;
import io.quarkus.arc.SyntheticCreationalContext;

/**
 * Stops a {@link MessageProcessor} synthetic CDI bean when its {@code @Singleton} context is destroyed, i.e. on
 * application shutdown. One bean is registered per channel by {@code JetStreamProcessor}.
 */
public class MessageProcessorBeanDestroyer implements BeanDestroyer<MessageProcessor> {

    @Override
    public void destroy(MessageProcessor instance, SyntheticCreationalContext<MessageProcessor> context) {
        if (instance instanceof MessagePublisherProcessor<?> publisherProcessor) {
            publisherProcessor.stop();
        } else if (instance instanceof MessageSubscriberProcessor<?> subscriberProcessor) {
            subscriberProcessor.stop();
        }
    }
}
