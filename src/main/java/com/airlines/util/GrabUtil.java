package com.airlines.util;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Slf4j
public class GrabUtil {

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
