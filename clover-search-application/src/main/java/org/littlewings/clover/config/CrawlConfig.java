package org.littlewings.clover.config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CrawlConfig {
    @Inject
    @ConfigProperty(name = "diary.archive.base.url")
    String baseUrl;

    @Inject
    @ConfigProperty(name = "diary.crawl.sleep.seconds")
    long crawlSleepSeconds;

    @Inject
    @ConfigProperty(name = "diary.crawl.job.schedule")
    String crawlJobSchedule;

    public static String retrieveResourceManagerStrategy() {
        return retrieveProperty("undertow.resource.manager.strategy", String.class);
    }

    static <T> T retrieveProperty(String propertyName, Class<T> propertyType) {
        return ConfigProvider.getConfig().getValue(propertyName, propertyType);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public long getCrawlSleepSeconds() {
        return crawlSleepSeconds;
    }

    public String getCrawlJobSchedule() {
        return crawlJobSchedule;
    }
}
