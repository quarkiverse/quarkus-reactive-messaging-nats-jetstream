package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;


import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import jakarta.annotation.Priority;
import org.eclipse.microprofile.config.spi.Converter;

import java.util.Arrays;
import java.util.List;

@Priority(100)
public class ConsumerConfigurationListConverter implements Converter<List<ConsumerConfiguration>> {

    private final ConsumerConfigurationConverter singleConverter = new ConsumerConfigurationConverter();

    @Override
    public List<ConsumerConfiguration> convert(String value) throws IllegalArgumentException, NullPointerException {
        if (value == null || value.isEmpty()) {
            return List.of();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .map(singleConverter::convert)
                .toList();
    }

}
