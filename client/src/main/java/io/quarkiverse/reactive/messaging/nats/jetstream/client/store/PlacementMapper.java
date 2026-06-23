package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import java.util.List;
import java.util.Optional;

import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;

@Mapper
interface PlacementMapper {

    Placement map(io.nats.client.api.Placement placement);

    @ObjectFactory
    default Placement create(io.nats.client.api.Placement placement) {
        return Placement.builder().tags(placement.getTags() != null ? placement.getTags() : List.of())
                .cluster(Optional.ofNullable(placement.getCluster())).build();
    }
}
