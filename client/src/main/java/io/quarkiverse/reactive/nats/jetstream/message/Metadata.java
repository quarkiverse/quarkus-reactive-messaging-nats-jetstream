package io.quarkiverse.reactive.nats.jetstream.message;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface Metadata {

    @NonNull Optional<Duration> acknowledgeTimeout();

    @NonNull List<Duration> acknowledgeBackoff();

    int deliveredCount();
}
