package com.airlines.util;

import cn.hutool.core.date.DateUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Pool;

import java.util.concurrent.*;

@Slf4j
public class GrabUtil {

    int nThreads = Runtime.getRuntime().availableProcessors();
    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("demo-pool-%d").build();

    ExecutorService pool = new ThreadPoolExecutor(5, 200,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws InterruptedException {


        for (int i = 0; i < 10; i++) {
            int finalI = i;
            executor.submit(() -> {
                String date = DateUtil.offsetDay(DateUtil.date(), finalI * 3).toString("yyyyMMdd");
                log.info("日期：" + date);
            });
        }

        try {
            // 等待所有任务完成
            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn("线程池未在60秒内完成所有任务");
            }
        } catch (InterruptedException e) {
            log.warn("线程池关闭时被中断", e);
            Thread.currentThread().interrupt();
        }
    }

}
