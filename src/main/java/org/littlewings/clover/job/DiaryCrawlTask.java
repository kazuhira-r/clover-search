package org.littlewings.clover.job;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import org.littlewings.clover.service.DiaryCrawlService;

@ApplicationScoped
public class DiaryCrawlTask {
    @Inject
    DiaryCrawlService diaryCrawlService;

    void onStart(@Observes StartupEvent ev) {
        execute();
    }

    @Scheduled(cron = "{diary.crawl.job.schedule}")
    public void execute() {
        diaryCrawlService.refresh();
    }
}
