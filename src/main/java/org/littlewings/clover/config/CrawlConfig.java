package org.littlewings.clover.config;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class CrawlConfig {
    @Inject
    @ConfigProperty(name = "diary.archive.base.url")
    private String archiveBaseUrl;

    @Inject
    @ConfigProperty(name = "diary.crawl.sleep.seconds")
    private long crawlSleepSeconds;

    @Inject
    @ConfigProperty(name = "diary.crawl.job.schedule")
    private String crawlJobSchedule;

    public String getArchiveBaseUrl() {
        return archiveBaseUrl;
    }

    public long getCrawlSleepSeconds() {
        return crawlSleepSeconds;
    }

    public String getCrawlJobSchedule() {
        return crawlJobSchedule;
    }
}
