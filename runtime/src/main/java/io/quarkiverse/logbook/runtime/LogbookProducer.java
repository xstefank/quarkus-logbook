package io.quarkiverse.logbook.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.zalando.logbook.Logbook;

import io.quarkus.arc.DefaultBean;

@Singleton
public class LogbookProducer {

    @Produces
    @ApplicationScoped
    @DefaultBean
    public Logbook logbook() {
        return Logbook.create();
    }
}
