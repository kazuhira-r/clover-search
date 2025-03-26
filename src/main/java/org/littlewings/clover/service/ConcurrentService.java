package org.littlewings.clover.service;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.Future;

@ApplicationScoped
public class ConcurrentService {
    @Resource
    private ManagedExecutorService executorService;

    public Future<?> submit(Runnable runnable) {
        return executorService.submit(runnable);
    }
}
