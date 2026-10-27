package io.quarkiverse.logbook.runtime;

import static io.quarkus.runtime.annotations.ConfigPhase.BUILD_TIME;

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.logbook")
@ConfigRoot(phase = BUILD_TIME)
public interface LogbookConfig {

    /**
     * Whether Logbook request/response logging should be enabled.
     */
    @WithDefault("true")
    boolean enabled();
}
