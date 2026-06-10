package io.quarkiverse.reactive.messaging.nats.test.fetch;

import java.time.Duration;
import java.util.Optional;

public record FetchConfiguration()
        implements
        io.quarkiverse.reactive.messaging.nats.client.consumer.FetchConfiguration {

    @Override
    public Optional<Duration> timeout() {
        return Optional.of(Duration.ofSeconds(3));
    }

    @Override
    public Integer batchSize() {
        return 10;
    }

}
