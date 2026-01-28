package cn.yoaoso.threadpool.core.executor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Drayd
 * @description 线程池执行器
 * @create 2026-01-16 23:36
 */
@Slf4j
@Getter
public class TidePoolExecutor extends ThreadPoolExecutor {
    /**
     * 线程池唯一标识，用来动态变更参数
     */
    private final String threadPoolId;


    /**
     * 线程池拒绝策略执行次数
     */
    private final AtomicLong rejectCount = new AtomicLong();

    /**
     * 等待终止时间，单位毫秒
     */
    private long awaitTerminationMillis;

    public TidePoolExecutor(
            @NonNull String threadPoolId,
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            @NonNull TimeUnit unit,
            @NonNull BlockingQueue<Runnable> workQueue,
            @NonNull ThreadFactory threadFactory,
            @NonNull RejectedExecutionHandler handler,
            long awaitTerminationMillis) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);

        //TODO 通过动态代理设置拒绝策略执行次数
        setRejectedExecutionHandler(handler);

        //设置动态线程池扩展属性：线程池ID标识
        this.threadPoolId = threadPoolId;

        //设置等待终结时间，单位毫秒
        this.awaitTerminationMillis = awaitTerminationMillis;
    }


    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        RejectedExecutionHandler handlerWrapper = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                rejectCount.incrementAndGet();
                handler.rejectedExecution(r, executor);

            }

            @Override
            public String toString() {
                return handler.getClass().getSimpleName();
            }
        };

        super.setRejectedExecutionHandler(handlerWrapper);
    }


}
