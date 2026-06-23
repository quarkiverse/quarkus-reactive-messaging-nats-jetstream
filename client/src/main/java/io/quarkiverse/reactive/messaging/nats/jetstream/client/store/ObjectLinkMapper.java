package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.mapstruct.Mapper;

@Mapper(uses = OptionalMapper.class)
public interface ObjectLinkMapper {

    ObjectLink map(io.nats.client.api.ObjectLink objectLink);

    default io.nats.client.api.ObjectLink map(ObjectLink objectLink) {
        if (objectLink.objectName().isPresent()) {
            return io.nats.client.api.ObjectLink.object(objectLink.bucket(), objectLink.objectName().get());
        } else {
            return io.nats.client.api.ObjectLink.bucket(objectLink.bucket());
        }
    }
}
