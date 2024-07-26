package com.xiaohub.util;

import java.util.concurrent.*;

public class ThreadPoolUtil {

    private static final int CORE_POOL_SIZE = 3;
    private static final int MAX_POOL_SIZE = 5;
    private static final long KEEP_ALIVE_TIME = 1L;
    private static final TimeUnit UNIT = TimeUnit.MINUTES;
    private static final BlockingDeque<Runnable> WORK_QUEUE = new LinkedBlockingDeque<>(100);

    private static final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
    private static final ExecutorService executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, UNIT, WORK_QUEUE, new ThreadPoolExecutor.CallerRunsPolicy());

    public static ScheduledFuture<?> scheduledAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(ThreadPoolUtil::shutdown));
    }

    /**
     * 提交一个需要返回值的Callable任务
     * 当需要返回结果，如数据库查询或复杂计算
     *
     * @param <T>  任务返回结果的类型
     * @param task 要执行的Callable任务
     * @return Future<T> 表示挂起的结果的Future，可用于获取结果
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    /**
     * 提交一个不需要返回值的Runnable任务
     * 当需要在后台执行一个不需要返回结果的操作，例如日志记录或发送通知
     *
     * @param task 要执行的Runnable任务
     * @return Future<?> 可用于取消执行的Future
     */
    public static Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    private static void shutdown() {
        try {
            executor.shutdown();
            scheduledExecutor.shutdown();
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
