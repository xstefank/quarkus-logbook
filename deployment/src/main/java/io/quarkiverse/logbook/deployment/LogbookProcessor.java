package io.quarkiverse.logbook.deployment;

import java.util.Optional;
import java.util.logging.Level;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkiverse.logbook.runtime.LogbookConfig;
import io.quarkiverse.logbook.runtime.LogbookJaxRsFilter;
import io.quarkiverse.logbook.runtime.LogbookProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LogCategoryBuildItem;
import io.quarkus.resteasy.common.spi.ResteasyJaxrsProviderBuildItem;
import io.quarkus.resteasy.reactive.spi.ContainerRequestFilterBuildItem;
import io.quarkus.resteasy.reactive.spi.ContainerResponseFilterBuildItem;
import io.quarkus.resteasy.reactive.spi.WriterInterceptorBuildItem;

class LogbookProcessor {

    private static final String FEATURE = "logbook";
    private static final String LOGBOOK_LOG_CATEGORY = "org.zalando.logbook";
    private static final String LOGBOOK_LOG_LEVEL_PROPERTY = "quarkus.log.category.\"" + LOGBOOK_LOG_CATEGORY + "\".level";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerBean(LogbookConfig config, BuildProducer<AdditionalBeanBuildItem> additionalBeans,
            BuildProducer<LogCategoryBuildItem> logCategories) {
        if (!config.enabled()) {
            return;
        }

        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClasses(LogbookProducer.class, LogbookJaxRsFilter.class)
                .setUnremovable()
                .build());

        // Quarkus clamps any category below the global build-time floor (DEBUG by default) up to that
        // floor, even if the user explicitly configured a lower level themselves. We only raise the
        // floor for this one category if the user actually asked for a lower level - we never invent a
        // default of our own, so with no config at all Logbook stays silent, exactly as it would
        // without this extension. Reading the raw String first is required: unconfigured categories
        // resolve to the literal value "inherit", which the Level converter can't parse.
        Config runtimeConfig = ConfigProvider.getConfig();
        Optional<String> rawLevel = runtimeConfig.getOptionalValue(LOGBOOK_LOG_LEVEL_PROPERTY, String.class);
        if (rawLevel.isPresent() && !"inherit".equalsIgnoreCase(rawLevel.get())) {
            Level level = runtimeConfig.getValue(LOGBOOK_LOG_LEVEL_PROPERTY, Level.class);
            logCategories.produce(new LogCategoryBuildItem(LOGBOOK_LOG_CATEGORY, level, true));
        }
    }

    // the filter is CDI-managed, not part of the app's own Jandex index, so it must be registered
    // explicitly with whichever JAX-RS implementation(s) are present - it won't be picked up by
    // plain @Provider annotation scanning

    @BuildStep(onlyIf = RestEasyClassicDetector.class)
    void registerForClassic(LogbookConfig config, BuildProducer<ResteasyJaxrsProviderBuildItem> classicProviders) {
        if (config.enabled()) {
            classicProviders.produce(new ResteasyJaxrsProviderBuildItem(LogbookJaxRsFilter.class.getName()));
        }
    }

    @BuildStep(onlyIf = RestEasyReactiveDetector.class)
    void registerForReactive(LogbookConfig config,
            BuildProducer<ContainerRequestFilterBuildItem> requestFilters,
            BuildProducer<ContainerResponseFilterBuildItem> responseFilters,
            BuildProducer<WriterInterceptorBuildItem> writerInterceptors) {
        if (!config.enabled()) {
            return;
        }

        String filterClassName = LogbookJaxRsFilter.class.getName();
        requestFilters.produce(new ContainerRequestFilterBuildItem(filterClassName));
        responseFilters.produce(new ContainerResponseFilterBuildItem.Builder(filterClassName).build());
        writerInterceptors.produce(new WriterInterceptorBuildItem.Builder(filterClassName).build());
    }
}
