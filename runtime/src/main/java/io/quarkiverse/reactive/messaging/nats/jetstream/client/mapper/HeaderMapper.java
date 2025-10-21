package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import java.util.List;
import java.util.Map;

import io.nats.client.impl.Headers;

public interface HeaderMapper {

    Map<String, List<String>> map(Headers headers);

}
