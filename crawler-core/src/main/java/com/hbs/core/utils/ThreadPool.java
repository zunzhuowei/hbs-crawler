package com.hbs.core.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zun.wei on 2019/4/11 16:19.
 * Description: 线程池
 */
public class ThreadPool {



    //参数初始化
    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    //核心线程数量大小;io型线程 cpuCore * 2 +1;计算型 cpuCore + 1
    private static final int corePoolSize = CPU_COUNT * 2 + 1;
    //线程池最大容纳线程数
    private static final int maximumPoolSize = corePoolSize * 2;
    //线程空闲后的存活时长
    private static final int keepAliveTime = 30;

    private static final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

    //线程的创建工厂
    private static final ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "Crawler #" + mCount.getAndIncrement());
        }
    };

    private static final RejectedExecutionHandler rejectHandler = new ThreadPoolExecutor.CallerRunsPolicy();

    //线程池对象，创建线程
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            corePoolSize, //核心线程数最大值
            maximumPoolSize,//线程总数最大值
            keepAliveTime,//非核心线程闲置超时时长
            TimeUnit.SECONDS,//非核心线程闲置超时时长 单位
            workQueue, //阻塞任务队列
            threadFactory, //新建线程工厂
            rejectHandler //当提交任务数超过maxmumPoolSize+workQueue之和时，任务会交给RejectedExecutionHandler来处理
    );

    public static void execute(Runnable command) {
        while (true) {
            if (ThreadPool.THREAD_POOL_EXECUTOR.getQueue().size() > 100) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            break;
        }
        THREAD_POOL_EXECUTOR.execute(command);
    }


}
