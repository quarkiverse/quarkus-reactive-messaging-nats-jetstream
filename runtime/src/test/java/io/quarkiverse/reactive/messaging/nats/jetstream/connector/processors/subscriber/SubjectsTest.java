package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.subscriber;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SubjectsTest {

    @ParameterizedTest
    @CsvSource({
            "data, data, true",
            "data, other, false",
            "data.*, data.a, true",
            "data.*, data.a.b, false",
            "data.*, data, false",
            "data.>, data.a, true",
            "data.>, data.a.b, true",
            "data.>, data, false",
            "*.a, data.a, true",
            "*.a, data.b, false",
            ">, data.a, true",
            "data.a, data.a.b, false",
    })
    void matches(String pattern, String subject, boolean expected) {
        assertThat(Subjects.matches(pattern, subject)).isEqualTo(expected);
    }
}
