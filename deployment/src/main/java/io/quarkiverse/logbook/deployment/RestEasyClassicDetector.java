package io.quarkiverse.logbook.deployment;

final class RestEasyClassicDetector extends ClasspathDetector {

    RestEasyClassicDetector() {
        super("io.quarkus.resteasy.common.runtime.ResteasyContextProvider");
    }
}
