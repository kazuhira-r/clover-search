package org.littlewings.clover.config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CrawlConfig {
    @Inject
    @ConfigProperty(name = "diary.archive.base.url")
    String baseUrl;

    @Inject
    @ConfigProperty(name = "diary.crawl.sleep.seconds")
    long crawlSleepSeconds;

    public String getBaseUrl() {
        return baseUrl;
    }

    public long getCrawlSleepSeconds() {
        return crawlSleepSeconds;
    }
}
