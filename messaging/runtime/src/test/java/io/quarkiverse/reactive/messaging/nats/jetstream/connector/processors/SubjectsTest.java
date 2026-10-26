package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Subjects;

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

    @ParameterizedTest
    @CsvSource({
            "data, data, true",
            "data, other, false",
            "data.>, data.a, true",
            "data.>, data., true",
            "data.>, data.a.b, true",
            "data.>, data.*, true",
            "data.>, data.>, true",
            "data.>, data, false",
            "data.>, accounting.a, false",
            "data.*, data.>, false",
            "data.*, data.*, true",
            "data.*, data.a, true",
            "data.*, data.a.b, false",
            "data.a, data.*, false",
            "account.*.orders.>, account.*.orders.>, true",
            "account.*.orders.>, account.42.orders.>, true",
            "account.*.orders.>, account.42.orders.created, true",
            "account.*.orders.>, account.42.orders, false",
            "account.*.orders.>, account.*.billing.>, false",
            "account.*.orders.>, account.>, false",
            "account.*.orders.>, account, false",
            "account.*.orders.>, account.*, false",
            "account.>, account.*.orders.>, true",
            "*.a, *.a, true",
            "*.a, b.a, true",
            "*.a, b.c, false",
            ">, data.*.x, true",
            ">, >, true",
            "data.a, data.a.b, false",
            "data.a.b, data.a, false",
    })
    void covers(String pattern, String other, boolean expected) {
        assertThat(Subjects.covers(pattern, other)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "data, data",
            "data.*, data.a",
            "data.>, data.a.b",
            "*.a, data.a",
            ">, data.a",
            "data.a, data.a.b",
            "data, other",
            "data.>, data",
    })
    void coversConcreteSubjectsLikeMatches(String pattern, String subject) {
        assertThat(Subjects.covers(pattern, subject)).isEqualTo(Subjects.matches(pattern, subject));
    }
}
