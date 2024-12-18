package io.quarkiverse.reactive.messaging.nats.jetstream.test.resources;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessageMetadata;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;

@ApplicationScoped
public class DataConsumingBean implements MessageConsumer<String> {
    private final static Logger logger = Logger.getLogger(DataConsumingBean.class);

    private final Emitter<Data> dataEmitter;

    public DataConsumingBean(@Channel("data-emitter") Emitter<Data> dataEmitter) {
        this.dataEmitter = dataEmitter;
    }

    @Incoming("data-consumer")
    public Uni<Void> data(Message<String> message) {
        return Uni.createFrom().item(message)
                .onItem().invoke(m -> logger.infof("Received message: %s", message))
                .onItem().transformToUni(this::publish)
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(throwable -> notAcknowledge(message, throwable));
    }

    private Uni<Message<String>> publish(Message<String> message) {
        try {
            return Uni.createFrom()
                    .item(() -> message.getMetadata(SubscribeMessageMetadata.class)
                            .map(metadata -> Tuple2.of(metadata.headers().get("RESOURCE_ID").get(0), metadata.messageId()))
                            .orElse(Tuple2.of(null, null)))
                    .onItem()
                    .transformToUni(tuple -> Uni.createFrom()
                            .completionStage(
                                    dataEmitter.send(new Data(message.getPayload(), tuple.getItem1(), tuple.getItem2()))))
                    .onItem().transform(ignore -> message);
        } catch (Exception e) {
            return Uni.createFrom().failure(e);
        }
    }
}
