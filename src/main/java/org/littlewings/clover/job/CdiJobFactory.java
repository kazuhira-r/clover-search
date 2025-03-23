package org.littlewings.clover.job;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

@ApplicationScoped
public class CdiJobFactory implements JobFactory {
    @SuppressWarnings("unchecked")
    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        Class<Job> jobClass = (Class<Job>) bundle.getJobDetail().getJobClass();
        return CDI.current().select(jobClass).get();
    }
}
