package org.littlewings.clover.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@ApplicationScoped
public class ConcurrentService {
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    public Future<?> submit(Runnable runnable) {
        return executorService.submit(runnable);
    }
}
