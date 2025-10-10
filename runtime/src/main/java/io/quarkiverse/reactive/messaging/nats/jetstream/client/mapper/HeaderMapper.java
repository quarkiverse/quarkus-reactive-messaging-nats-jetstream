package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import io.nats.client.impl.Headers;

import java.util.List;
import java.util.Map;

public interface HeaderMapper {

    Map<String, List<String>> map(Headers headers);

}
