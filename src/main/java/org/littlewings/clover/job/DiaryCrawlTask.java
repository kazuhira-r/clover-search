package org.littlewings.clover.job;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.littlewings.clover.entity.DiaryEntry;
import org.littlewings.clover.service.DiaryCrawlService;

@ApplicationScoped
public class DiaryCrawlTask {
    Logger logger = Logger.getLogger(DiaryCrawlTask.class);

    ExecutorService jobExecutorService = Executors.newSingleThreadExecutor();

    @Inject
    DiaryCrawlService diaryCrawlService;

    void onStart(@Observes StartupEvent ev) {
        execute();
    }

    @Scheduled(cron = "{diary.crawl.job.schedule}")
    public void execute() {
        Uni
                .createFrom()
                .item(() -> {
                    logger.infof("start scheduled crawling job...");

                    List<DiaryEntry> diaryEntries = diaryCrawlService.refresh();

                    logger.infof("end initialize crawling, entries count = %d", diaryEntries.size());

                    return null;
                })
                .runSubscriptionOn(jobExecutorService)
                .subscribe()
                .with(v -> {
                });
    }
}
