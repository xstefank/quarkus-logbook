package io.quarkiverse.logbook.it;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Runs in-process only (not extended by a *IT class): it attaches a log handler directly to the
 * JVM running the Quarkus app, which isn't possible against a separately packaged/native artifact.
 * <p>
 * No logging config is set here, so the root logger stays at its default level (INFO). Logbook must
 * therefore stay silent, exactly as it would without this extension - the extension must not force
 * its own category above whatever level the application actually configured.
 */
@QuarkusTest
public class LogbookLoggingTest {

    private static final Logger LOGBOOK_LOGGER = Logger.getLogger("org.zalando.logbook.Logbook");

    private final List<LogRecord> records = new CopyOnWriteArrayList<>();
    private Handler handler;
    private Level originalLevel;

    @BeforeEach
    void attachHandler() {
        // the JBoss LogManager logger is a JVM-wide singleton: a previous @QuarkusTest running under a
        // different profile (e.g. LogbookLoggingEnabledTest) may have left an explicit level on it that
        // survives the app restart, since our build step never touches this category without config
        originalLevel = LOGBOOK_LOGGER.getLevel();
        LOGBOOK_LOGGER.setLevel(null);

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
        LOGBOOK_LOGGER.setLevel(originalLevel);
    }

    @Test
    public void staysSilentAtTheDefaultRootLogLevel() {
        given()
                .when().get("/logbook")
                .then()
                .statusCode(200);

        assertTrue(records.isEmpty(),
                "expected no Logbook output at the default root log level (INFO), got: " + records);
    }
}
