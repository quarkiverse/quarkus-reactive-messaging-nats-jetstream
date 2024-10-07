/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.quarkiverse.reactive.messaging.nats.jetstream.it;

import java.util.HashMap;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import com.fasterxml.jackson.annotation.JsonView;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamOutgoingMessageMetadata;
import io.smallrye.mutiny.Uni;

@Path("/data")
@Produces("application/json")
@ApplicationScoped
public class DataResource {
    @Inject
    DataConsumingBean bean;

    @Channel("data")
    Emitter<Data> emitter;

    @GET
    @Path("/last")
    public Data getLast() {
        return bean.getLast().orElseGet(Data::new);
    }

    @GET
    @Path("/last-with-timestamp")
    @JsonView(IncludeTimestamps.class)
    public Data getLastWithTimestamp() {
        return bean.getLast().orElseGet(Data::new);
    }

    @POST
    @Consumes("application/json")
    @Path("/{messageId}")
    public Uni<Void> produceData(@PathParam("messageId") String messageId, Data data) {
        return Uni.createFrom().item(() -> emitData(messageId, data))
                .onItem().ignore().andContinueWithNull();
    }

    private Message<Data> emitData(String messageId, Data data) {
        final var headers = new HashMap<String, List<String>>();
        headers.put("RESOURCE_ID", List.of(data.getResourceId()));
        final var message = Message.of(data,
                Metadata.of(JetStreamOutgoingMessageMetadata.of(messageId, headers, null)));
        emitter.send(message);
        return message;
    }
}
