package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.*;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

public class HealthTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestSpanExporter.class, Data.class, DataResource.class, DataConsumingBean.class,
                            DataCollectorBean.class, MessageConsumer.class))
            .withConfigurationResource("application-health.properties");

    @Test
    public void readiness() {
        await().atMost(60, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> {
            try {
                given()
                        .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                        .when().get("/q/health/ready")
                        .then()
                        .statusCode(200);
                return true;
            } catch (AssertionError e) {
                return false;
            }
        });
    }

    @Test
    public void liveness() {
        await().atMost(60, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> {
            try {
                given()
                        .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                        .when().get("/q/health/live")
                        .then()
                        .statusCode(200);
                return true;
            } catch (AssertionError e) {
                return false;
            }
        });
    }

}
