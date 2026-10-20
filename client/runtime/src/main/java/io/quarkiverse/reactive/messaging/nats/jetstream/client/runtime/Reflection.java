package io.quarkiverse.reactive.messaging.nats.jetstream.client.runtime;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = { Byte[].class, Byte.class })
public class Reflection {
}
