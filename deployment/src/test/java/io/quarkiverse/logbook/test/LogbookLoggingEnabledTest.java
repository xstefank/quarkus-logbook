package io.quarkiverse.logbook.test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

/**
 * Runs in its own Maven module (a separate Surefire JVM fork from the integration-tests modules), so
 * explicitly enabling Logbook here can't leak JBoss LogManager state into the "silent by default" tests
 * living in the integration-tests modules, which assert the opposite (no config -> no logging).
 */
public class LogbookLoggingEnabledTest {

    @Path("/logbook-logging-enabled-test")
    @ApplicationScoped
    public static class TestResource {
        @GET
        public String hello() {
            return "Hello logbook";
        }
    }

    @RegisterExtension
    static final QuarkusUnitTest test = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClasses(TestResource.class))
            .overrideConfigKey("quarkus.log.category.\"org.zalando.logbook\".level", "TRACE");

    private static final Logger LOGBOOK_LOGGER = Logger.getLogger("org.zalando.logbook.Logbook");

    private final List<LogRecord> records = new CopyOnWriteArrayList<>();
    private Handler handler;

    @BeforeEach
    void attachHandler() {
        handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        LOGBOOK_LOGGER.addHandler(handler);
    }

    @AfterEach
    void detachHandler() {
        LOGBOOK_LOGGER.removeHandler(handler);
    }

    @Test
    public void logsRequestAndResponseOnceExplicitlyEnabled() {
        given()
                .when().get("/logbook-logging-enabled-test")
                .then()
                .statusCode(200);

        assertTrue(records.size() >= 2,
                "expected Logbook to log both the request and the response once TRACE is enabled, got: " + records);
    }
}
