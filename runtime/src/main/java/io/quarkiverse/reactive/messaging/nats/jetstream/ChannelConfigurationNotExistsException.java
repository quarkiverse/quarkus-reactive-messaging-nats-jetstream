package io.quarkiverse.reactive.messaging.nats.jetstream;

public class ChannelConfigurationNotExistsException extends RuntimeException {

    public ChannelConfigurationNotExistsException(String channel) {
        super(String.format("Channel %s not found", channel));
    }
}
