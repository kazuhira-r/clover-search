package org.littlewings.clover.config;

import java.util.Optional;
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

    public static String retrieveServerBindAddress() {
        return retrieveProperty("server.bind.address", String.class)
                .orElse("localhost");
    }

    public static int retrieveServerPort() {
        return retrieveProperty("server.bind.address", Integer.class)
                .orElse(8080);
    }

    public static String retrieveResourceManagerStrategy() {
        return retrieveProperty("undertow.resource.manager.strategy", String.class)
                .orElse("classpath");
    }


    static <T> Optional<T> retrieveProperty(String propertyName, Class<T> propertyType) {
        return ConfigProvider.getConfig().getOptionalValue(propertyName, propertyType);
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
