package org.littlewings.clover.job;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.littlewings.clover.config.CrawlConfig;
import org.littlewings.clover.service.ConcurrentService;
import org.littlewings.clover.service.DiaryCrawlService;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

@ApplicationScoped
public class DiaryCrawlTask implements Job {
    @Inject
    private CrawlConfig crawlConfig;

    @Inject
    private CdiJobFactory cdiJobFactory;

    @Inject
    private DiaryCrawlService diaryCrawlService;

    @Inject
    private ConcurrentService concurrentService;


    void onStart(@Observes Startup event) throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.setJobFactory(cdiJobFactory);

        JobDetail jobDetail = JobBuilder
                .newJob(DiaryCrawlTask.class)
                .withIdentity("diary-crawl-job", "group")
                .build();

        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("diary-crawl-job-trigger", "group")
                .withSchedule(CronScheduleBuilder.cronSchedule(crawlConfig.getCrawlJobSchedule()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.start();

        concurrentService.submit(() -> {
            try {
                execute(null);
            } catch (JobExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        diaryCrawlService.refresh();
    }
}
