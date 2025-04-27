package ru.hse.pensieve.database.cassandra.config;

import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class FeedTaskExecutor {
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 20;
    private static final ThreadPoolExecutor EXECUTOR =
            new ThreadPoolExecutor(
                    CORE_POOL_SIZE,
                    MAX_POOL_SIZE,
                    30L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(100),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

    public static Executor getExecutor() {
        return EXECUTOR;
    }
}