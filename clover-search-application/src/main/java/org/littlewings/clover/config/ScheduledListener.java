package org.littlewings.clover.config;

import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import javax.enterprise.inject.spi.CDI;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jboss.logging.Logger;
import org.littlewings.clover.entity.DiaryEntry;
import org.littlewings.clover.service.DiaryCrawlService;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class ScheduledListener implements ServletContextListener {
    Logger logger = Logger.getLogger(ScheduledListener.class);

    Scheduler scheduler;

    public void contextInitialized(ServletContextEvent sce) {
        CrawlConfig crawlConfig = CDI.current().select(CrawlConfig.class).get();

        CompletableFuture
                .runAsync(() -> {
                    logger.infof("start initialize crawling...");

                    DiaryCrawlService diaryCrawlService = CDI.current().select(DiaryCrawlService.class).get();

                    List<DiaryEntry> diaryEntries = diaryCrawlService.refresh();
                    logger.infof("end initialize crawling, entries count = %d", diaryEntries.size());
                })
                .exceptionally(throwable -> {
                    logger.errorf(throwable, "error in initialize crawling");
                    return null;
                });

        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        try {
            scheduler = schedulerFactory.getScheduler();

            JobDetail job =
                    JobBuilder
                            .newJob(DiaryCrawlJob.class)
                            .withIdentity("diary-crawl-job", "crawl-group")
                            .build();
            CronTrigger trigger =
                    TriggerBuilder
                            .newTrigger()
                            .withIdentity("diary-crawl-trigger", "crawl-group")
                            .withSchedule(
                                    CronScheduleBuilder
                                            .cronSchedule(crawlConfig.getCrawlJobSchedule())
                                            .inTimeZone(TimeZone.getTimeZone("Asia/Tokyo"))
                            )
                            .build();

            scheduler.scheduleJob(job, trigger);
            scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    public static class DiaryCrawlJob implements Job {
        Logger logger = Logger.getLogger(DiaryCrawlJob.class);

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            logger.infof("start scheduled crawling job...");

            DiaryCrawlService diaryCrawlService = CDI.current().select(DiaryCrawlService.class).get();
            List<DiaryEntry> diaryEntries = diaryCrawlService.refresh();

            logger.infof("end initialize crawling, entries count = %d", diaryEntries.size());
        }
    }
}
