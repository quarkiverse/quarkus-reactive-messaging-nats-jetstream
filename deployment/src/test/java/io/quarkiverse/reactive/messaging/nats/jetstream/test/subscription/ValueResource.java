package io.quarkiverse.reactive.messaging.nats.jetstream.test.subscription;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/value")
public class ValueResource {

    @Inject
    ValueConsumingBean bean;

    @GET
    @Path("/last")
    public long getLast() {
        return bean.getLastValue();
    }

}
