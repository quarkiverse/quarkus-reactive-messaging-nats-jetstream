package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

public abstract class AbstractSubscribeOptionsFactory {

    protected Optional<String[]> getBackOff(final MessagePublisherConfiguration configuration) {
        return configuration.getBackOff().map(backoff -> backoff.split(","));
    }

    protected Optional<Duration[]> getBackOff(String[] backoff) {
        if (backoff == null || backoff.length == 0) {
            return Optional.empty();
        } else {
            return Optional.of(Arrays.stream(backoff).map(this::toDuration).toList()
                    .toArray(new Duration[] {}));
        }
    }

    protected Duration toDuration(String value) {
        return Duration.parse(value);
    }
}
