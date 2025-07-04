package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.smallrye.config.common.utils.StringUtil;
import org.eclipse.microprofile.config.spi.Converter;

public class ConsumerConfigurationConverter implements Converter<ConsumerConfiguration<?>[]> {

    @Override
    public ConsumerConfiguration<?>[] convert(String value) throws IllegalArgumentException, NullPointerException {
        if (value.isEmpty()) {
            return null;
        } else {
            String[] itemStrings = StringUtil.split(value);

            return new ConsumerConfiguration<?>[0];
        }
    }
}
